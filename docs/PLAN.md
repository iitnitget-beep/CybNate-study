# DeepRoot — Master Plan
> UNBREAKABLE study app. One thing in life stays true: every session, we work Plan → Code → Verify → Push, in that order.
> This file is the brain of the project. Any chat, any day: read this file, do the next unchecked box.

## ⚙️ How every session works (rhythm for 1 hour/day)
1. Open the plan, pick the next unchecked box.
2. Buffy writes the code, verifies syntax/consistency.
3. Buffy commits + pushes to GitHub **deeproot branch**.
4. Faith runs the app in Android Studio, reports bugs.
5. Bugs get fixed before new features. Always.

## 🐾 Key facts
- Package: `com.deeproot.app` · Min SDK 26 · Target SDK 35 · Kotlin · Compose
- Firebase project: `deeproot-e4d17` · RTDB + Firestore + Auth + Storage
- ⚠️ SECURITY: never commit `.jks` keystore, API keys, or secrets
- Attack-surface rules: no local-auth fallbacks, no client-trusted scores, App Check everywhere, server-side scoring, enumeration-safe errors.

## 🗺️ Roadmap

### Phase 0 — Foundation (Day 1–2) ✅ TODAY
- [ ] Gradle KTS project, version catalog, min/target SDK
- [ ] google-services.json committed
- [ ] Hardened AndroidManifest (no backup, no cleartext, singleTask)
- [ ] ProGuard/R8 with keep rules
- [ ] Theme foundation (DeepRoot colors: deep green + gold accent)
- [ ] App icon + splash
**Faith does:** Firebase console → Authentication → enable Email/Google · create API key restrictions (AndroidApp + package)

### Phase 1 — Design System (Day 3–5)
- [ ] Dark-first theme, typography scale
- [ ] Button / Card / Sheet components with animations
- [ ] Navigation skeleton (list of screens)
- [ ] Empty/loading/error state components
**Faith does:** send 2 apps you like the look of (still waiting on this)

### Phase 2 — Auth & Data Model (Day 6–8)
- [ ] Email + Google sign-in screens (hardened: enumeration-safe errors, lockout, session manager)
- [ ] AuthRepository (no fake-login fallbacks, no local session trust)
- [ ] Firestore schema + hardened rules + App Check enforcement
- [ ] Onboarding (exam type, class, schedule builder)
**Faith does:** enable Firestore in console; App Check in monitor mode

### Phase 3 — Focus Engine (Day 9–13) 🧠 Core feature
- [ ] Deep-work session timer (Pomodoro++, with power-drain animation)
- [ ] Distraction shield: DND mode + 'phone down' whitelist
- [ ] Session → XP → streak (Gamified)
- [ ] Firestore 'focus_logs' collection + rules
**Faith does:** test timer on real phone, rotate screen, background app

### Phase 4 — Question Practice Engine (Day 14–18)
- [ ] Question bank schema & seeding (server-side)
- [ ] Practice screen (quick-fire Q&A, instant feedback)
- [ ] Weakness heatmap UI
- [ ] Spaced revision queue (Leitner system)
**Faith does:** review questions quality, report typos

### Phase 5 — Task & Schedule (Day 19–22)
- [ ] Daily auto-generated task list (mix of focus + practice + revision)
- [ ] Calendar view
- [ ] Notification system (daily task ping)
**Faith does:** check notification permission flow

### Phase 6 — AI Doubt Solver 🆓 (Day 23–25)
- [ ] Server-side AI callable (Ollama/Gemini, key server-only)
- [ ] Photo doubt capture + step-by-step solutions
- [ ] Hard rate limits (15/day per user server-side)
**Faith does:** set AI keys in Cloud Functions config (never in app!)

### Phase 7 — Analytics & Insights (Day 26–28)
- [ ] Weekly performance report (chart)
- [ ] Percentile estimate
- [ ] Server-side scoring (anti-cheat hardening of practice/focus)
**Faith does:** Firebase console enable App Check ENFORCEMENT

### Phase 8 — Polish + Bug Bash (Day 29–32)
- [ ] Rotation state preserved (rememberSaveable everywhere)
- [ ] Edge cases: no internet, empty states, permission denials
- [ ] Performance passes, release checklist
**Faith does:** full quality pass on your phone

### Phase 9 — Release (Day 33–35)
- [ ] Signed release build (R8)
- [ ] README + release notes
- [ ] Play Store listing (title, desc, screenshots)
**Faith does:** create keystore (guided), upload to Play Internal Test

## 🔒 Security cut (applied from Day 0, not at the end)
1. No auth credentials/fake-login in client — server is the only truth
2. R8 obfuscation ON, allowBackup=false, no cleartext, singleTask
3. App Check enforced on EVERY Firestore/Storage/Function call
4. Server-side scoring & validation (client scores are untrusted)
5. Enumeration-safe auth errors
6. Rate limiting on every expensive call (AI, writes)
7. No sensitive data in prefs/backup
8. Secrets NEVER in repo, manifest, or build config
