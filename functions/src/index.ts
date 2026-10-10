import * as admin from "firebase-admin";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { beforeUserCreated, HttpsError } from "firebase-functions/v2/identity";
import { onCall } from "firebase-functions/v2/https";

admin.initializeApp();
const db = admin.firestore();

/**
 * Triggered on Auth user creation: initializes default user profile doc in Firestore.
 */
export const onUserCreatedInit = beforeUserCreated(async (event) => {
  const user = event.data;
  if (!user) return;

  const userDocRef = db.collection("users").doc(user.uid);
  await userDocRef.set(
    {
      uid: user.uid,
      email: user.email || "",
      displayName: user.displayName || "Student",
      role: "student",
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true }
  );
});

/**
 * Callable function to securely assign role claims (Admin only)
 */
export const setCustomUserRole = onCall({ enforceAppCheck: true }, async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Must be logged in to set user roles.");
  }

  // Ensure caller is an admin
  const callerClaims = request.auth.token;
  if (!callerClaims.admin) {
    throw new HttpsError("permission-denied", "Only administrators can change roles.");
  }

  const { targetUid, role } = request.data;
  if (!targetUid || !role) {
    throw new HttpsError("invalid-argument", "targetUid and role are required.");
  }

  await admin.auth().setCustomUserClaims(targetUid, {
    admin: role === "admin",
    role: role,
  });

  return { success: true, message: `Role ${role} successfully granted to ${targetUid}` };
});

/**
 * Firestore trigger on new exam attempt to update global statistics
 */
export const onAttemptCreated = onDocumentCreated(
  "users/{userId}/attempts/{attemptId}",
  async (event) => {
    const snap = event.data;
    if (!snap) return;

    const attemptData = snap.data();
    const examId = (attemptData.examKey || attemptData.exam || "general") as string;

    const statsRef = db.collection("exam_analytics").doc(examId);
    await statsRef.set(
      {
        totalAttempts: admin.firestore.FieldValue.increment(1),
        lastAttemptTimestamp: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  }
);

// ============================================================================
// AI Doubt Solver - Secure backend proxy
// ----------------------------------------------------------------------------
// The provider API key (Gemini free tier OR Groq free tier OR your own
// self-hosted Ollama server) NEVER reaches the app. The app calls this
// callable function; we validate auth + App Check + per-user quota, then
// forward the question to the provider with the key from server-side config.
// Switching providers later = redeploy functions, zero app changes.
// ============================================================================

// Round-based quota: max N AI questions per user per rolling 24h window.
const AI_DAILY_LIMIT = 25;
const MAX_QUESTION_CHARS = 2000;

/** Per-user 24h quota counter stored in Firestore (reads are free, never exposed to client). */
async function checkAndIncrAiQuota(uid: string): Promise<{ allowed: boolean; remaining: number }> {
  const now = Date.now();
  const dayMs = 24 * 60 * 60 * 1000;
  const ref = db.collection("_internal_ai_limits").doc(uid);
  const allowed = await db.runTransaction(async (tx) => {
    const doc = await tx.get(ref);
    const data = doc.data();
    const windowStart = data?.windowStart as number | undefined;
    const count = (data?.count as number | undefined) ?? 0;

    if (!windowStart || now - windowStart > dayMs) {
      tx.set(ref, { windowStart: now, count: 1 }, { merge: true });
      return { allowed: true, remaining: AI_DAILY_LIMIT - 1 };
    }
    if (count >= AI_DAILY_LIMIT) {
      return { allowed: false, remaining: 0 };
    }
    tx.set(ref, { count: count + 1 }, { merge: true });
    return { allowed: true, remaining: AI_DAILY_LIMIT - count - 1 };
  });
  return allowed;
}

/** Single provider-agnostic call. Returns just the answer text. */
async function callProvider(prompt: string): Promise<string> {
  const geminiApiKey = process.env.GEMINI_API_KEY;
  const groqApiKey = process.env.GROQ_API_KEY;
  const ollamaUrl = process.env.OLLAMA_URL; // e.g. http://your-vms-ip:11434 (self-hosted open-weight)

  // 1) Gemini (Google AI Studio free tier)
  if (geminiApiKey) {
    const model = process.env.GEMINI_MODEL || "gemini-2.0-flash";
    const resp = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${geminiApiKey}`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          contents: [{ parts: [{ text: prompt }] }],
          generationConfig: { temperature: 0.2, maxOutputTokens: 1200 },
        }),
      }
    );
    if (!resp.ok) throw new HttpsError("unavailable", `AI provider error (${resp.status})`);
    const data = await resp.json();
    const text: string | undefined = data?.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!text) throw new HttpsError("internal", "AI returned no answer");
    return text;
  }

  // 2) Groq (free tier, OpenAI-compatible endpoint, very fast inference)
  if (groqApiKey) {
    const model = process.env.GROQ_MODEL || "llama-3.3-70b-versatile";
    const resp = await fetch("https://api.groq.com/openai/v1/chat/completions", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${groqApiKey}`,
      },
      body: JSON.stringify({
        model,
        messages: [{ role: "user", content: prompt }],
        temperature: 0.2,
        max_tokens: 1200,
      }),
    });
    if (!resp.ok) throw new HttpsError("unavailable", `AI provider error (${resp.status})`);
    const data = await resp.json();
    const text: string | undefined = data?.choices?.[0]?.message?.content;
    if (!text) throw new HttpsError("internal", "AI returned no answer");
    return text;
  }

  // 3) Self-hosted open-weight model (Gemma 3 / Llama 3.2 / Phi-3 via Ollama).
  // Optional OLLAMA_TOKEN: if set, your Ollama proxy must receive this bearer
  // token so strangers cannot use your server. Set it on your Ollama box
  // (nginx/caddy in front) with the same value.
  if (ollamaUrl) {
    const model = process.env.OLLAMA_MODEL || "gemma3:4b";
    const token = process.env.OLLAMA_TOKEN;
    const headers: Record<string, string> = { "Content-Type": "application/json" };
    if (token) headers["Authorization"] = `Bearer ${token}`;
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 55000);
    try {
      const resp = await fetch(`${ollamaUrl.replace(/\/$/, "")}/api/generate`, {
        method: "POST",
        headers,
        body: JSON.stringify({ model, prompt, stream: false }),
        signal: controller.signal,
      });
      if (!resp.ok) throw new HttpsError("unavailable", `AI provider error (${resp.status})`);
      const data = await resp.json();
      const text: string | undefined = data?.response;
      if (!text) throw new HttpsError("internal", "AI returned no answer");
      return text;
    } catch (e) {
      if (e instanceof HttpsError) throw e;
      throw new HttpsError("unavailable", "Your AI server is offline. Try again later.");
    } finally {
      clearTimeout(timer);
    }
  }

  throw new HttpsError(
    "failed-precondition",
    "AI is not configured yet. Set GEMINI_API_KEY or GROQ_API_KEY or OLLAMA_URL as a server-side secret."
  );
}

/**
 * Callable: solves an exam doubt with a step-by-step explanation.
 * Hard guards: signed-in + App Check enforced + per-user quota + input limits.
 */
export const askAiDoubt = onCall(
  {
    enforceAppCheck: true,
    region: "us-central1",
    memory: "512MiB",
    timeoutSeconds: 60,
  },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "Sign in first.");
    }

    const q = (request.data?.question as string | undefined) ?? "";
    if (!q || typeof q !== "string" || q.length < 5 || q.length > MAX_QUESTION_CHARS) {
      throw new HttpsError("invalid-argument", "Question must be 5-2000 characters.");
    }
    const options = Array.isArray(request.data?.options)
      ? (request.data.options as string[]).filter(
          (o: unknown): o is string => typeof o === "string" && o.length <= 300
        ).slice(0, 6)
      : [];
    const exam = typeof request.data?.exam === "string" ? request.data.exam.slice(0, 50) : "";
    const subject = typeof request.data?.subject === "string" ? request.data.subject.slice(0, 50) : "";

    const uid = request.auth.uid;
    const quota = await checkAndIncrAiQuota(uid);
    if (!quota.allowed) {
      throw new HttpsError(
        "resource-exhausted",
        "Daily AI doubt limit reached (25). Quota resets every 24 hours."
      );
    }

    const optionsBlock = options.length
      ? `Options:\n${options.map((o: string, i: number) => `${String.fromCharCode(65 + i)}) ${o}`).join("\n")}\n`
      : "";
    const contextBlock = exam || subject
      ? ` (exam: ${exam || "any"}${subject ? `, subject: ${subject}` : ""})`
      : "";

    const prompt =
      `You are an expert ${exam || "competitive exam"} tutor preparing students${contextBlock}. ` +
      `Solve the following doubt with a clear, step-by-step explanation a student can follow. ` +
      `Keep it under 500 words. Use simple language.\n\n` +
      `Question:\n${q}\n${optionsBlock}` +
      (options.length ? "End with the correct option letter and why it is correct." : "");

    const answer = await callProvider(prompt);
    return { answer, remaining: quota.remaining };
  }
);
