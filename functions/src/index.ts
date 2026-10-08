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
    const examId = attemptData.examId || "general";

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
