# CybNate — Zero-Cost Backend Setup & Security Checklist

Every step below is FREE. Nothing here changes or exposes your existing
credentials (keystore, google-services.json, API IDs all stay untouched).

---

## 1. Choose your AI backend (pick ONE — all free)

| Option | Cost | Quality | Setup |
|---|---|---|---|
| **Gemini API free tier** (recommended to start) | ₹0 | High | 2 min |
| **Groq free tier** (Llama 3.3 70B — truly open-source model) | ₹0 | High, fastest | 2 min |
| **Self-hosted open-weight model** (Gemma 3, Llama 3.2, Phi-3 via Ollama) | ₹0 forever | Medium | needs your own always-on VM (Oracle Always Free works) |

The deployed Cloud Function auto-detects which key exists and uses it.
All three paths go through the SAME function call — no app changes needed
to switch later.

### Gemini (fastest to set up)
1. Go to https://aistudio.google.com/apikey → create API key (free, no billing card needed).
2. Add it as a **server-side secret** (never in the app!):
   ```bash
   firebase functions:secrets:set GEMINI_API_KEY
   firebase functions:secrets:set GEMINI_MODEL   # optional, e.g. gemini-2.0-flash
   ```
3. Deploy: `firebase deploy --only functions`

### Groq (100% open-source model, free)
1. Sign up at https://console.groq.com → API Keys → create key.
2. ```bash
   firebase functions:secrets:set GROQ_API_KEY
   firebase functions:secrets:set GROQ_MODEL   # e.g. llama-3.3-70b-versatile
   firebase deploy --only functions
   ```

### Self-hosted open-source models (100% free forever, no API key needed)
Run Ollama with a small open-weight model on CPU — your own machine or a
free VM. No GPU purchase needed, no credit card needed for local.

1. **On your always-on PC/laptop/VM:**
```bash
curl -fsSL https://ollama.com/install.sh | sh
ollama pull llama3.2:3b     # or gemma3:4b / phi3 (choose based on your RAM)
```
RAM guide: 1B-3B models need ~4-8 GB free; 7B needs ~8-16 GB, all on CPU.

2. **Expose Ollama safely with a small auth-gate proxy** (so only your
   Cloud Function can use it, nobody else). Save this as `ollama-proxy.js`
   on the same machine:
```javascript
// Needs Node 18+. Run: OLLAMA_TOKEN=<your-secret> node ollama-proxy.js
const http = require("http");
const https = require("https");
const SECRET = process.env.OLLAMA_TOKEN;
if (!SECRET) { console.error("set OLLAMA_TOKEN!"); process.exit(1); }

http.createServer((req, res) => {
  const auth = req.headers.authorization || "";
  if (auth !== `Bearer ${SECRET}`) {
    res.writeHead(403).end("Forbidden"); return;
  }
  let body = "";
  req.on("data", c => body += c);
  req.on("end", () => {
    const proxyReq = https.request(
      { host: "127.0.0.1", port: 11434, path: "/api/generate", method: "POST",
        headers: { "Content-Type": "application/json" } },
      pr => { res.writeHead(pr.statusCode); pr.pipe(res); }
    );
    proxyReq.on("error", () => res.writeHead(502).end());
    proxyReq.end(body);
  });
}).listen(8443, () => console.log("ollama proxy listening on 8443"));
```
Run it, open firewall port 8443 only, note your machine's public IP.

3. **Wire it to Firebase (server-side only, never in the app):**
```bash
firebase functions:secrets:set OLLAMA_URL     # http://<your-public-ip>:8443
firebase functions:secrets:set OLLAMA_TOKEN   # same value you used above
firebase functions:secrets:set OLLAMA_MODEL   # llama3.2:3b
firebase deploy --only functions
```

---

## 2. Enable App Check enforcement (blocks non-app hackers)

The app and the backend ALREADY send/verify App Check. You only need to
turn on enforcement in the console:

1. Firebase Console → your project (`deeproot-e4d17`) → **App Check**.
2. Under **Apps**, register your Android app with Play Integrity (release)
   and Debug token `a78f2e2d-3c99-4d62-b91c-7f5b89a3dfcb` (debug).
3. Under **Products → Cloud Firestore**, click **Enforce** (start with
   "Monitor" for a few days, watch the console metrics, then Enforce).
4. Do the same for **Cloud Functions** (callables) — the function already
   has `enforceAppCheck: true`, so this just clicks the console switch.
5. Deploy the updated rules so content reads require App Check too:
   ```bash
   firebase deploy --only firestore:rules
   ```

## 3. Lock Firestore to production

Already done in `firestore.rules`:
- exam_analytics is now **not readable/writable by any client** (server only)
- All exam content requires a verified app (App Check) to read
- User data can only be touched by its owner
Deploy it:
```bash
firebase deploy --only firestore:rules,firestore:indexes
```

## 4. OTP / Phone auth

Fixed in this update:
- 60-second resend cooldown with visual countdown (stops "too many requests" errors)
- Friendly error messages for quota/throttle/invalid-number/weak-OTP cases
- Lockout stays enforced (no more auto-reset on app start)

⚠️ One thing only YOU can enable: real SMS in production.
Firebase Console → Authentication → Sign-in method → Phone:
- Add your app's SHA-256 fingerprint (release keystore) for reCAPTCHA-free flow.
- Firebase free tier includes ~10 SMS/day for testing numbers only.
- For real users at scale, you must add billing for Auth SMS OR use
  Google/Email sign-in as primary (both free, both already in your app).
Tip: add your own number under Authentication → Settings → Phone numbers
for testing, so you can test OTP free forever.

## 5. What's already protected (from this session)

| Attack | Defense in place |
|---|---|
| Fake logins / auth bypass | Removed local login fallbacks; enumeration-safe errors |
| Brute-force password attempts | 5-attempt lockout, no auto-reset |
| API key theft / APK leaks | No AI/provider keys in app; all in server secrets |
| Non-app clients hitting APIs | App Check enforced in function + rules |
| Quota drain / abuse of AI | 25/day per user, server-enforced |
| Data tampering in Firestore | Field-level validation in rules, analytics write-blocked |
| Metadata leaks in backups | allowBackup=false, session/lockout prefs excluded |
| Reverse engineering | R8 obfuscation + resource shrinking enabled |

## 6. Secrets hygiene (do this once)

Never put any of these in code, git, or the app:
- GEMINI_API_KEY / GROQ_API_KEY — only via `functions:secrets:set`
- debug.keystore / release keystore — already .gitignore'd
- google-services.json — public identifiers, fine to keep as-is

Run this after deploying functions once:
```bash
firebase functions:log --limit 50   # verify askAiDoubt has no errors
```

## 7. Deploy order

```bash
git pull
cd functions && npm install && cd ..
firebase deploy --only firestore:rules
firebase functions:secrets:set GEMINI_API_KEY   # or GROQ_API_KEY / OLLAMA_URL
firebase deploy --only functions
# Build + install debug APK → App Check debug token works out of the box
# Build release → Play Integrity takes over automatically
```
