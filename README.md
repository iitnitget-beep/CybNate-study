# Marks - Competitive Exam Test Series & Real-Time Analytics App

A mobile-first quiz and mock test application inspired by the **Marks** app (by ScoreExam / MathonGo), designed for JEE, NEET, and competitive examination preparation. Built natively on Android using Kotlin, Jetpack Compose, Material Design 3, Firebase Authentication, and Cloud Firestore.

Connected directly to Firebase project: **`deeproot-e4d17`**.

---

## 🚀 How Dynamic Exam Branches Work (JEE, NEET, and Future Exams)

The app features a **dynamic, multi-exam branching engine**. It automatically listens to your Firestore in real time:
- Whenever you upload new questions or new tests under **JEE**, they immediately appear in the **JEE** section.
- Whenever you upload new questions or new tests under **NEET**, they immediately appear in the **NEET** section.
- Whenever you create a new exam branch in future (e.g. **BITSAT**, **CUET**, **NDA**, **UPSC**), a dedicated tab automatically appears on the app's home screen!

### How to Add a New Exam or Branch in Firestore

You can add a new exam branch in **either** of two simple ways:

#### Option A: Dedicated `exams` Collection (Recommended)
Add a document to `exams/{examId}` (e.g. `exams/bitsat`):
```json
{
  "name": "BITSAT",
  "displayName": "BITSAT Engineering Mock",
  "icon": "computer",
  "order": 3,
  "description": "Pilani & Goa campus entrance exam"
}
```

#### Option B: Simply tag your test with the exam name
When you create a document in `tests/{testId}`, include the `exam` field:
```json
{
  "title": "BITSAT Full Mock Test #1",
  "exam": "BITSAT",
  "categoryId": "<categoryId>",
  "duration": 60,
  "totalMarks": 120.0,
  "negativeMarking": 1.0,
  "totalQuestions": 30,
  "difficulty": "Medium"
}
```
The app will dynamically discover `"BITSAT"` and present it as a separate exam tab automatically!

---

## 📝 How to Upload Questions to Firestore

Questions can be added to the root `questions` collection:
Collection: `questions/{questionId}`
```json
{
  "testId": "<testId>",
  "exam": "JEE",
  "text": "A particle moves in a circle of radius 20 cm with a linear speed of 10 m/s. What is its angular velocity?",
  "options": [
    "25 rad/s",
    "50 rad/s",
    "100 rad/s",
    "200 rad/s"
  ],
  "correctIndex": 1,
  "explanation": "Angular velocity ω = v / r = 10 m/s / 0.20 m = 50 rad/s.",
  "marks": 4.0,
  "imageUrl": "",
  "order": 1
}
```
> **Note**: `correctIndex` is 0-indexed: `0` for Option A, `1` for Option B, `2` for Option C, and `3` for Option D.

---

## 🗄️ Firestore Collections Summary

- **`exams/{examId}`**: Exam branches (e.g. `name`: "JEE", "NEET", "BITSAT")
- **`categories/{categoryId}`**: Subjects/chapters (e.g. `name`: "Physics", `exam`: "JEE")
- **`tests/{testId}`**: Mock test papers (e.g. `title`, `exam`: "JEE"|"NEET", `duration`, `totalMarks`, `negativeMarking`, `totalQuestions`)
- **`questions/{questionId}`**: Question statements, 4 options, correct answer index, and step-by-step explanations
- **`users/{userId}/attempts/{attemptId}`**: Attempt results, question timings, and scores
- **`users/{userId}/bookmarks/{bookmarkId}`**: Bookmarked questions saved for revision

---

## 📱 App Flow & Features

1. **Exam Selector**: Switch effortlessly between **JEE**, **NEET**, and any newly added exams with live test counters.
2. **Subject/Chapter Filter**: Filter tests by Physics, Chemistry, Mathematics, Biology, etc.
3. **Instructions Screen**: Full breakdown of duration, marking rules (+4 / -1), and question palette guide.
4. **Distraction-Free Test Engine**:
   - Single question at a time with clear single-choice options
   - Real-time countdown timer with auto-submission when time runs out
   - Clear response & Mark for review
   - Color-coded Question Palette drawer
   - Submit confirmation dialog
5. **Instant Analytics**: Score, accuracy %, correct/incorrect/skipped breakdown, and time spent.
6. **Solution Review**: Question-by-question review with filters (*All*, *Wrong*, *Correct*, *Skipped*), detailed explanations, and question bookmarking.
7. **Profile & Revision**: Review past attempts and revise bookmarked questions anytime.
