# DeepRoot — Your console to-do list (Day 1)

Do these once, in order. ~10 minutes total. All free.

## 1. Firebase console (console.firebase.google.com → project DeepRoot)
- [ ] **Authentication → Sign-in method**: enable **Email/Password** and **Google**
- [ ] **Firestore Database → Create database** (production mode, region: pick nearest to India, e.g. asia-south1)
- [ ] **App Check → Apps → register**: Play Integrity (already wired in code), get the app listed
- [ ] **App Check → Firestore/Functions**: set to **Monitor** for now (we flip to Enforce in Phase 7)
- [ ] **Settings → API keys → restrict**: application restriction = Android apps; add package `com.deeproot.app` + SHA-1 already registered ✔

## 2. Android Studio (your machine)
- [ ] `git clone` this repo (branch: `deeproot`)  — or `git pull` if already cloned
- [ ] Open the project, let Gradle sync
- [ ] Run on device/emulator → you should see "DeepRoot — Phase 0 scaffold OK"

## 3. Before release (Phase 9, not now)
- [ ] Play Integrity needs the app published to internal testing first — later.
