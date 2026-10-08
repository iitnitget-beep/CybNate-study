package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.Attempt
import com.example.data.model.Bookmark
import com.example.data.model.Category
import com.example.data.model.ExamBranch
import com.example.data.model.Question
import com.example.data.model.Test
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MarksRepository(private val db: FirebaseFirestore) {

    companion object {
        fun resolveFirestore(context: Context): FirebaseFirestore {
            val dbId = try {
                context.applicationContext.getString(R.string.firestore_database_id)
            } catch (e: Exception) {
                ""
            }
            return if (dbId.isBlank() || dbId == "(default)") {
                FirebaseFirestore.getInstance()
            } else {
                FirebaseFirestore.getInstance(dbId)
            }
        }
    }

    constructor(context: Context) : this(resolveFirestore(context))

    private val auth = Firebase.auth

    // In-memory store for guest mode attempts and bookmarks
    private val guestAttempts = MutableStateFlow<List<Attempt>>(emptyList())
    private val guestBookmarks = MutableStateFlow<List<Bookmark>>(emptyList())

    private val imgRegex = Regex("""<img[^>]+src=["']([^"']+)["'][^>]*>""", RegexOption.IGNORE_CASE)
    private val htmlTagRegex = Regex("""<[^>]+>""")

    private fun extractImgUrl(input: String?): String {
        return extractAllImgUrls(input).firstOrNull() ?: ""
    }

    private fun extractAllImgUrls(input: String?): List<String> {
        if (input.isNullOrBlank()) return emptyList()
        val results = mutableListOf<String>()
        imgRegex.findAll(input).forEach { match ->
            val url = match.groupValues[1].trim()
            if (url.isNotBlank() && !results.contains(url)) {
                results.add(url)
            }
        }
        val trimmed = input.trim()
        if (results.isEmpty() && (trimmed.startsWith("http://") || trimmed.startsWith("https://"))) {
            results.add(trimmed)
        }
        return results
    }

    private fun cleanHtmlText(input: String?): String {
        if (input.isNullOrBlank()) return ""
        var text = com.example.util.MathFormatter.convertMathML(input)
        text = text.replace(imgRegex, "")
        text = text.replace("<br>", "\n")
            .replace("<br/>", "\n")
            .replace("<br />", "\n")
            .replace("</p>", "\n")
            .replace("<p>", "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
        text = htmlTagRegex.replace(text, "")
        return text.trim()
    }

    /**
     * Real-time observation of Exam Branches directly from Firestore `Exams` collection
     * (and fallback `exams`).
     */
    fun observeExamBranches(): Flow<List<ExamBranch>> = callbackFlow {
        var examsList = emptyList<ExamBranch>()

        fun emitCombined() {
            val sorted = examsList.distinctBy { it.name.lowercase() }.sortedBy { it.order }
            if (sorted.isEmpty()) {
                trySend(listOf(
                    ExamBranch(id = "neet", name = "NEET", displayName = "NEET", order = 1),
                    ExamBranch(id = "jee_main", name = "JEE", displayName = "JEE Main", order = 2)
                ))
            } else {
                trySend(sorted)
            }
        }

        val examsListener = db.collection("Exams").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("MarksRepo", "Error listening to Exams: ${error.message}")
            }
            val list = mutableListOf<ExamBranch>()
            snapshot?.documents?.forEach { doc ->
                val rawName = doc.getString("name") ?: doc.id
                val normalizedName = if (rawName.contains("jee", ignoreCase = true)) "JEE" else if (rawName.contains("neet", ignoreCase = true)) "NEET" else rawName
                val displayName = doc.getString("displayName") ?: normalizedName
                val order = (doc.get("order") as? Number)?.toInt() ?: if (normalizedName == "NEET") 1 else 2
                list.add(ExamBranch(id = doc.id, name = normalizedName, displayName = displayName, order = order))
            }
            if (list.isNotEmpty()) {
                examsList = list
                emitCombined()
            }
        }

        val examsLowerListener = db.collection("exams").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null && !snapshot.isEmpty) {
                val list = mutableListOf<ExamBranch>()
                snapshot.documents.forEach { doc ->
                    val rawName = doc.getString("name") ?: doc.id
                    val normalizedName = if (rawName.contains("jee", ignoreCase = true)) "JEE" else if (rawName.contains("neet", ignoreCase = true)) "NEET" else rawName
                    val displayName = doc.getString("displayName") ?: normalizedName
                    val order = (doc.get("order") as? Number)?.toInt() ?: if (normalizedName == "NEET") 1 else 2
                    list.add(ExamBranch(id = doc.id, name = normalizedName, displayName = displayName, order = order))
                }
                examsList = (examsList + list).distinctBy { it.name.lowercase() }
                emitCombined()
            }
        }

        emitCombined()

        awaitClose {
            examsListener.remove()
            examsLowerListener.remove()
        }
    }

    /**
     * Real-time observation of categories (subjects) from Firestore `Exams` collection
     * with tailored real curriculum subjects for NEET (Chemistry, Zoology, Botany, Physics) and JEE.
     * Note: Fake "Biology" section is completely removed for NEET.
     */
    fun observeCategories(examBranch: String? = null): Flow<List<Category>> = callbackFlow {
        val isNeet = examBranch?.contains("neet", true) == true
        val targetExamKey = if (isNeet) "NEET" else "JEE_Main"

        val defaultCategories = if (isNeet) {
            listOf(
                Category(id = "neet_chem", name = "Chemistry", exam = "NEET", order = 1),
                Category(id = "neet_zoo", name = "Zoology", exam = "NEET", order = 2),
                Category(id = "neet_bot", name = "Botany", exam = "NEET", order = 3),
                Category(id = "neet_phys", name = "Physics", exam = "NEET", order = 4)
            )
        } else {
            listOf(
                Category(id = "jee_phys", name = "Physics", exam = "JEE", order = 1),
                Category(id = "jee_chem", name = "Chemistry", exam = "JEE", order = 2),
                Category(id = "jee_math", name = "Mathematics", exam = "JEE", order = 3)
            )
        }

        var categoriesFromExams = emptyList<Category>()
        var categoriesFromRoot = emptyList<Category>()

        fun emitCombined() {
            val allRemote = (categoriesFromExams + categoriesFromRoot)
            val filteredRemote = if (isNeet) {
                allRemote.filter {
                    // Strictly exclude any fake "Biology" category for NEET
                    !it.name.equals("Biology", true) && !it.id.equals("bio", true) && !it.id.equals("biology", true)
                }
            } else {
                allRemote
            }
            val combined = (filteredRemote + defaultCategories)
                .distinctBy { it.name.trim().lowercase() }
                .sortedBy { it.order }
            trySend(combined)
        }

        emitCombined()

        // 1. Listen to Exams/{exam}/Subjects
        val lExamsSubjects = db.collection("Exams").document(targetExamKey)
            .collection("Subjects").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapIndexedNotNull { idx, doc ->
                        val rawName = doc.getString("name") ?: doc.getString("title") ?: doc.id
                        val normName = when {
                            rawName.contains("chem", true) -> "Chemistry"
                            rawName.contains("zoo", true) -> "Zoology"
                            rawName.contains("botan", true) -> "Botany"
                            rawName.contains("phys", true) -> "Physics"
                            rawName.contains("math", true) -> "Mathematics"
                            isNeet && rawName.contains("bio", true) -> null // Strictly remove fake Biology
                            else -> rawName
                        }
                        if (normName == null) null
                        else Category(id = doc.id, name = normName, exam = targetExamKey, order = idx + 1)
                    }
                    categoriesFromExams = list
                    emitCombined()
                }
            }

        // 2. Also listen to root categories if any
        val lRootCategories = db.collection("categories").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    val cat = doc.toObject(Category::class.java)?.copy(id = doc.id)
                    val examTag = doc.getString("exam") ?: doc.getString("branch") ?: ""
                    cat?.copy(exam = if (cat.exam.isBlank()) examTag else cat.exam)
                }.filter {
                    val catExam = it.exam.ifBlank { examBranch ?: "" }
                    if (isNeet) {
                        catExam.contains("neet", true) && !it.name.equals("Biology", true)
                    } else if (examBranch != null) {
                        catExam.contains(examBranch, true)
                    } else true
                }
                categoriesFromRoot = list
                emitCombined()
            }
        }

        awaitClose {
            lExamsSubjects.remove()
            lRootCategories.remove()
        }
    }

    /**
     * Real-time observation of tests (chapters) directly from Firestore `Exams` collection
     * hierarchy, `collectionGroup("Chapters")`, and dynamically synthesized from uploaded questions.
     * Note: In NEET, all chapters are strictly mapped to real curriculum subjects (Botany, Zoology, Chemistry, Physics).
     */
    fun observeTests(examBranch: String? = null, categoryId: String? = null): Flow<List<Test>> = callbackFlow {
        var testsFromRootTests = emptyList<Test>()
        var testsFromRootChapters = emptyList<Test>()
        var testsFromExams = emptyList<Test>()
        var testsFromCollectionGroup = emptyList<Test>()
        var testsFromQuestions = emptyList<Test>()

        fun parseTestDocument(doc: com.google.firebase.firestore.DocumentSnapshot): Test? {
            val title = doc.getString("name") ?: doc.getString("title") ?: doc.getString("chapter") ?: doc.getString("chapter_name") ?: doc.id
            val parentSubjDoc = doc.reference.parent.parent
            val parentExamDoc = parentSubjDoc?.parent?.parent
            val rawExam = doc.getString("exam") ?: doc.getString("branch") ?: parentExamDoc?.id ?: "NEET"
            val normExam = if (rawExam.contains("jee", true)) "JEE" else if (rawExam.contains("neet", true)) "NEET" else rawExam
            val rawSubj = doc.getString("subject") ?: doc.getString("category") ?: doc.getString("sub") ?: parentSubjDoc?.id ?: ""
            val normSubj = when {
                rawSubj.contains("botan", true) -> "Botany"
                rawSubj.contains("zoo", true) -> "Zoology"
                rawSubj.contains("chem", true) -> "Chemistry"
                rawSubj.contains("phys", true) -> "Physics"
                rawSubj.contains("math", true) -> "Mathematics"
                normExam.contains("neet", true) && rawSubj.contains("bio", true) -> "Botany"
                else -> rawSubj.ifBlank { if (normExam.contains("neet", true)) "Botany" else "Physics" }
            }
            val totalQ = (doc.get("totalQuestions") as? Number)?.toInt()
                ?: (doc.get("questionCount") as? Number)?.toInt()
                ?: (doc.get("total_questions") as? Number)?.toInt()
                ?: 45
            val duration = (doc.get("duration") as? Number)?.toInt()
                ?: (doc.get("timeMinutes") as? Number)?.toInt()
                ?: 60
            val marks = (doc.get("totalMarks") as? Number)?.toDouble()
                ?: (doc.get("marks") as? Number)?.toDouble()
                ?: (totalQ * 4.0)

            return Test(
                id = doc.id,
                title = title,
                chapter = title,
                subject = normSubj,
                exam = normExam,
                examKey = if (normExam.contains("neet", true)) "NEET" else if (normExam.contains("jee", true)) "JEE_Main" else normExam,
                categoryId = normSubj.lowercase(),
                totalQuestions = totalQ,
                duration = duration,
                totalMarks = marks
            )
        }

        fun emitCombined() {
            val allTests = (testsFromRootTests + testsFromRootChapters + testsFromCollectionGroup + testsFromExams + testsFromQuestions).distinctBy {
                "${it.exam.lowercase()}_${it.subject.lowercase()}_${it.chapter.lowercase().ifBlank { it.title.lowercase() }}"
            }
            var filtered = allTests

            if (!examBranch.isNullOrBlank() && !examBranch.equals("ALL", ignoreCase = true)) {
                val isNeet = examBranch.contains("neet", true)
                filtered = filtered.filter {
                    if (isNeet) {
                        it.exam.contains("neet", true) || it.branch.contains("neet", true) ||
                        it.subject.contains("botan", true) || it.subject.contains("zoo", true) ||
                        it.subject.contains("chem", true) || it.subject.contains("phys", true)
                    } else {
                        it.exam.isBlank() || it.exam.contains(examBranch, ignoreCase = true) || it.branch.contains(examBranch, ignoreCase = true)
                    }
                }
            }

            if (!categoryId.isNullOrBlank()) {
                val normalizedCatId = categoryId.lowercase()
                filtered = filtered.filter {
                    val testCatId = it.categoryId.lowercase()
                    val testSubj = it.subject.lowercase()
                    testCatId == normalizedCatId ||
                    testCatId.contains(normalizedCatId) ||
                    normalizedCatId.contains(testCatId) ||
                    (normalizedCatId.contains("botan") && (testSubj.contains("botan") || testCatId.contains("botan"))) ||
                    (normalizedCatId.contains("zoo") && (testSubj.contains("zoo") || testCatId.contains("zoo"))) ||
                    (normalizedCatId.contains("chem") && (testSubj.contains("chem") || testCatId.contains("chem"))) ||
                    (normalizedCatId.contains("phys") && (testSubj.contains("phys") || testCatId.contains("phys"))) ||
                    (normalizedCatId.contains("math") && (testSubj.contains("math") || testCatId.contains("math")))
                }
            }

            // Real Firestore chapters only - NO FAKE FALLBACK DATA
            trySend(filtered.sortedBy { it.title })
        }

        val listeners = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()

        // 1. Root collections "tests" and "Tests"
        listOf("tests", "Tests").forEach { colName ->
            val l = db.collection(colName).addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { parseTestDocument(it) }
                    testsFromRootTests = (testsFromRootTests + list).distinctBy { it.id }
                    emitCombined()
                }
            }
            listeners.add(l)
        }

        // 2. Root collections "chapters" and "Chapters"
        listOf("chapters", "Chapters").forEach { colName ->
            val l = db.collection(colName).addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { parseTestDocument(it) }
                    testsFromRootChapters = (testsFromRootChapters + list).distinctBy { it.id }
                    emitCombined()
                }
            }
            listeners.add(l)
        }

        // 3. Targeted query on Exams/{examKey}/Subjects and dynamically listen only to selected subject's Chapters
        val isNeet = examBranch?.contains("neet", true) == true
        val targetExamKey = if (examBranch.isNullOrBlank() || isNeet) "NEET" else if (examBranch.contains("jee", true)) "JEE" else examBranch
        val activeChapterListeners = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()

        val subjectsListener = db.collection("Exams").document(targetExamKey)
            .collection("Subjects")
            .addSnapshotListener { subjectsSnap, error ->
                if (error == null && subjectsSnap != null) {
                    activeChapterListeners.forEach { it.remove() }
                    activeChapterListeners.clear()

                    val targetDocs = if (!categoryId.isNullOrBlank()) {
                        val normCat = categoryId.lowercase()
                        subjectsSnap.documents.filter { doc ->
                            val sName = doc.getString("name") ?: doc.id
                            sName.lowercase().contains(normCat) || normCat.contains(sName.lowercase()) ||
                            (normCat.contains("botan") && sName.lowercase().contains("botan")) ||
                            (normCat.contains("zoo") && sName.lowercase().contains("zoo")) ||
                            (normCat.contains("chem") && sName.lowercase().contains("chem")) ||
                            (normCat.contains("phys") && sName.lowercase().contains("phys")) ||
                            (normCat.contains("math") && sName.lowercase().contains("math"))
                        }.ifEmpty { subjectsSnap.documents }
                    } else {
                        subjectsSnap.documents
                    }

                    targetDocs.forEach { subjDoc ->
                        val chapListener = subjDoc.reference.collection("Chapters")
                            .addSnapshotListener { chapSnap, chapErr ->
                                if (chapErr == null && chapSnap != null && !chapSnap.isEmpty) {
                                    val list = chapSnap.documents.mapNotNull { parseTestDocument(it) }
                                    testsFromExams = (testsFromExams + list).distinctBy { it.id }
                                    emitCombined()
                                }
                            }
                        activeChapterListeners.add(chapListener)
                    }
                }
            }
        listeners.add(subjectsListener)

        emitCombined()

        awaitClose {
            activeChapterListeners.forEach { it.remove() }
            listeners.forEach { it.remove() }
        }
    }

    /**
     * Real-time observation of available topic sub-branches for a given chapter.
     */
    fun observeTopicsForChapter(test: Test): Flow<List<String>> = callbackFlow {
        val isNeet = test.exam.contains("neet", true) || test.examKey.contains("neet", true)
        val examKey = if (test.examKey.isNotBlank()) test.examKey else if (isNeet) "NEET" else if (test.exam.contains("jee", true)) "JEE_Main" else test.exam
        val subject = test.subject.ifBlank {
            when {
                test.categoryId.contains("botan", true) -> "Botany"
                test.categoryId.contains("zoo", true) -> "Zoology"
                test.categoryId.contains("chem", true) -> "Chemistry"
                test.categoryId.contains("phys", true) -> "Physics"
                test.categoryId.contains("math", true) -> "Mathematics"
                isNeet -> "Botany"
                else -> "Physics"
            }
        }
        val chapter = if (test.chapter.isNotBlank()) test.chapter else test.title

        val topicsFromSubcollections = mutableSetOf<String>()
        val topicsFromRootQuestions = mutableSetOf<String>()

        fun emitCombinedTopics() {
            val fromFirestore = (topicsFromSubcollections + topicsFromRootQuestions)
                .filter { it.isNotBlank() && !it.equals("General", true) && !it.equals("All", true) }
                .sorted()
            val curated = getCuratedTopicsForChapter(test)
            val combined = (fromFirestore + curated).distinct().filter { it.isNotBlank() }
            val result = listOf("All Topics") + combined
            trySend(result)
        }

        emitCombinedTopics()

        // 1. Listen to Topics subcollection under chapter in Exams hierarchy
        val topicsSubListener = if (subject.isNotBlank() && chapter.isNotBlank()) {
            db.collection("Exams").document(examKey)
                .collection("Subjects").document(subject)
                .collection("Chapters").document(chapter)
                .collection("Topics")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        snapshot.documents.forEach { doc ->
                            val name = doc.getString("name") ?: doc.getString("title") ?: doc.id
                            if (name.isNotBlank()) topicsFromSubcollections.add(name)
                        }
                        emitCombinedTopics()
                    }
                }
        } else null

        // 2. Listen to root questions for topic field
        val rootQuestionsListener = db.collection("questions")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    snapshot.documents.filter { matchesChapter(it, test) }.forEach { doc ->
                        val top = doc.getString("topic") ?: doc.getString("topic_name") ?: doc.getString("subtopic") ?: ""
                        if (top.isNotBlank()) topicsFromRootQuestions.add(top)
                    }
                    emitCombinedTopics()
                }
            }

        // 3. Listen to uppercase Questions for topic field
        val rootQuestionsUpperListener = db.collection("Questions")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    snapshot.documents.filter { matchesChapter(it, test) }.forEach { doc ->
                        val top = doc.getString("topic") ?: doc.getString("topic_name") ?: doc.getString("subtopic") ?: ""
                        if (top.isNotBlank()) topicsFromRootQuestions.add(top)
                    }
                    emitCombinedTopics()
                }
            }

        awaitClose {
            topicsSubListener?.remove()
            rootQuestionsListener.remove()
            rootQuestionsUpperListener.remove()
        }
    }

    /**
     * Check if a Firestore document belongs to this chapter/test using resilient, fuzzy matching.
     */
    private fun matchesChapter(doc: com.google.firebase.firestore.DocumentSnapshot, test: Test): Boolean {
        val docTestId = doc.getString("testId") ?: doc.getString("test_id") ?: doc.getString("testID") ?: ""
        if (docTestId.isNotBlank() && (docTestId == test.id || docTestId.equals(test.id, ignoreCase = true))) {
            return true
        }

        val cleanTargetChapter = test.chapter.ifBlank { test.title }.trim().lowercase()
            .replace(Regex("""[^a-z0-9]"""), " ").replace(Regex("""\s+"""), " ").trim()

        val docChap = (doc.getString("chapter")
            ?: doc.getString("chapter_name")
            ?: doc.getString("chapterName")
            ?: doc.getString("unit")
            ?: doc.getString("testTitle")
            ?: doc.getString("topic")
            ?: "").trim().lowercase()
            .replace(Regex("""[^a-z0-9]"""), " ").replace(Regex("""\s+"""), " ").trim()

        if (cleanTargetChapter.isNotBlank() && docChap.isNotBlank()) {
            if (cleanTargetChapter == docChap) return true
            if (docChap.contains(cleanTargetChapter) || cleanTargetChapter.contains(docChap)) return true

            // Match major keywords (e.g. "cell", "genetics", "kinematics")
            val targetWords = cleanTargetChapter.split(" ").filter { it.length >= 4 }
            val docWords = docChap.split(" ").filter { it.length >= 4 }
            if (targetWords.isNotEmpty() && docWords.isNotEmpty() && targetWords.any { docWords.contains(it) }) {
                return true
            }
        }

        // Check if test was dynamically synthesized from questions
        if (test.id.startsWith("q_discovered_")) {
            val docExam = doc.getString("exam") ?: doc.getString("branch") ?: ""
            val isTargetExam = (test.exam.contains("neet", true) && docExam.contains("neet", true)) ||
                               (test.exam.contains("jee", true) && docExam.contains("jee", true)) ||
                               test.exam.isBlank()
            if (isTargetExam && cleanTargetChapter.isNotBlank() && docChap.isNotBlank()) {
                if (docChap.contains(cleanTargetChapter) || cleanTargetChapter.contains(docChap)) return true
            }
        }

        return false
    }

    /**
     * Standard curriculum topic sub-branches for competitive exam chapters.
     */
    private fun getCuratedTopicsForChapter(test: Test): List<String> {
        val name = (test.chapter.ifBlank { test.title }).lowercase()
        return when {
            name.contains("cell") -> listOf(
                "Cell Theory & Organelles",
                "Plasma Membrane & Transport",
                "Endomembrane System",
                "Nucleus & Chromatin",
                "Prokaryotic vs Eukaryotic Cells"
            )
            name.contains("inheritance") || name.contains("genetics") -> listOf(
                "Mendelian Laws & Monohybrid Cross",
                "Dihybrid Cross & Recombination",
                "Incomplete Dominance & Codominance",
                "Chromosomal Disorders",
                "Pedigree Analysis & Sex Determination"
            )
            name.contains("motion") || name.contains("kinematics") -> listOf(
                "Uniform Motion & Speed",
                "Kinematic Equations (1D)",
                "Motion under Gravity",
                "Relative Velocity",
                "Displacement & Velocity-Time Graphs"
            )
            name.contains("organic") -> listOf(
                "IUPAC Nomenclature",
                "Inductive & Resonance Effects",
                "Hyperconjugation & Carbocations",
                "Structural & Stereo Isomerism",
                "Reaction Intermediates"
            )
            name.contains("calculus") || name.contains("limit") || name.contains("continuity") -> listOf(
                "Standard Limits & L'Hopital's Rule",
                "Continuity of Functions",
                "Differentiability & Modulus Functions",
                "Trigonometric & Exponential Limits"
            )
            name.contains("bonding") -> listOf(
                "VSEPR Theory & Molecular Geometry",
                "Hybridization & Bond Angles",
                "Dipole Moment & Polarity",
                "Hydrogen Bonding & Lattice Energy"
            )
            name.contains("rotation") -> listOf(
                "Moment of Inertia Theorems",
                "Torque & Angular Acceleration",
                "Conservation of Angular Momentum",
                "Rolling Motion & Kinetic Energy"
            )
            name.contains("matrices") || name.contains("determinant") -> listOf(
                "Matrix Multiplication & Transpose",
                "Determinant Properties",
                "Adjoint & Inverse of Matrices",
                "System of Linear Equations"
            )
            else -> listOf(
                "Core Concepts & Formulas",
                "Key Problem-Solving Techniques",
                "Previous Years High-Yield PYQs",
                "Advanced Multi-Concept Practice"
            )
        }
    }

    /**
     * Real-time observation of questions with topic and difficulty sub-branch filtering.
     * Listens to Firestore `Exams` hierarchy, subcollections, collection groups, and root questions.
     * Provides high-yield, exam-accurate questions so students are NEVER blocked with an empty screen.
     */
    fun observeQuestions(test: Test, topic: String? = null, difficulty: String? = null): Flow<List<Question>> = callbackFlow {
        val isNeet = test.exam.contains("neet", true) || test.examKey.contains("neet", true)
        val examKey = if (test.examKey.isNotBlank()) test.examKey else if (isNeet) "NEET" else if (test.exam.contains("jee", true)) "JEE_Main" else test.exam
        val subject = test.subject.ifBlank {
            when {
                test.categoryId.contains("botan", true) -> "Botany"
                test.categoryId.contains("zoo", true) -> "Zoology"
                test.categoryId.contains("chem", true) -> "Chemistry"
                test.categoryId.contains("phys", true) -> "Physics"
                test.categoryId.contains("math", true) -> "Mathematics"
                isNeet -> "Botany"
                else -> "Physics"
            }
        }
        val chapter = if (test.chapter.isNotBlank()) test.chapter else test.title

        Log.d("MarksRepo", "Observing questions real-time: examKey=$examKey, subject=$subject, chapter=$chapter, topic=$topic, diff=$difficulty")

        val listeners = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()
        val questionsMap = mutableMapOf<String, List<Question>>()

        fun emitCombined() {
            val firestoreQuestions = questionsMap.values.flatten().distinctBy { it.id }.sortedBy { it.order }

            // Real Firestore questions only - NO FAKE FALLBACK QUESTIONS
            val baseList = firestoreQuestions

            var result = baseList

            // 1. Topic filtering
            if (!topic.isNullOrBlank() && !topic.equals("All", true) && !topic.equals("All Topics", true)) {
                val normTopic = topic.trim().lowercase()
                val topicMatches = baseList.filter { q ->
                    q.topic.lowercase().contains(normTopic) || normTopic.contains(q.topic.lowercase())
                }
                if (topicMatches.isNotEmpty()) {
                    result = topicMatches
                }
            }

            // 2. Difficulty filtering (Easy / Medium / Hard)
            if (!difficulty.isNullOrBlank() && !difficulty.equals("All", true)) {
                val targetDiff = when (difficulty.trim().lowercase()) {
                    "easy", "beginner", "simple" -> "Easy"
                    "hard", "tough", "advanced" -> "Hard"
                    else -> "Medium"
                }
                val diffMatches = result.filter { q ->
                    q.difficulty.equals(targetDiff, ignoreCase = true)
                }
                if (diffMatches.isNotEmpty()) {
                    result = diffMatches
                }
            }

            trySend(result)
        }

        // 1. Real-time listener on direct chapter Questions subcollections in Exams
        if (subject.isNotBlank() && chapter.isNotBlank()) {
            val chapterDocRef = db.collection("Exams").document(examKey)
                .collection("Subjects").document(subject)
                .collection("Chapters").document(chapter)

            // Direct subcollection Questions (uppercase)
            val lDirectQuestionsUpper = chapterDocRef.collection("Questions")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapIndexedNotNull { index, doc ->
                            parseFirestoreQuestion(doc, test.id, test.exam, index)
                        }
                        questionsMap["hierarchical_direct_upper"] = list
                        emitCombined()
                    }
                }
            listeners.add(lDirectQuestionsUpper)

            // Direct subcollection questions (lowercase)
            val lDirectQuestionsLower = chapterDocRef.collection("questions")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapIndexedNotNull { index, doc ->
                            parseFirestoreQuestion(doc, test.id, test.exam, index)
                        }
                        questionsMap["hierarchical_direct_lower"] = list
                        emitCombined()
                    }
                }
            listeners.add(lDirectQuestionsLower)

            // Listen to Chapter document itself (if questions array is stored directly on the chapter document)
            val lChapterDoc = chapterDocRef.addSnapshotListener { docSnap, error ->
                if (error == null && docSnap != null && docSnap.exists()) {
                    val qListRaw = docSnap.get("questions") as? List<*>
                    if (qListRaw != null && qListRaw.isNotEmpty()) {
                        val parsedQuestions = mutableListOf<Question>()
                        qListRaw.forEachIndexed { idx, item ->
                            if (item is Map<*, *>) {
                                val qText = (item["text"] as? String) ?: (item["question"] as? String) ?: ""
                                val optsRaw = item["options"] as? List<*>
                                val opts = optsRaw?.map { it?.toString() ?: "" } ?: emptyList()
                                val corrRaw = item["correctIndex"] ?: item["answer"] ?: item["correct"] ?: 0
                                val corrIdx = when (corrRaw) {
                                    is Number -> corrRaw.toInt().let { if (it in 1..4) it - 1 else it.coerceIn(0, 3) }
                                    is String -> when (corrRaw.trim().uppercase()) {
                                        "B", "2" -> 1
                                        "C", "3" -> 2
                                        "D", "4" -> 3
                                        else -> 0
                                    }
                                    else -> 0
                                }
                                if (qText.isNotBlank()) {
                                    parsedQuestions.add(
                                        Question(
                                            id = "${docSnap.id}_q_$idx",
                                            testId = test.id,
                                            exam = test.exam,
                                            subject = subject,
                                            chapter = chapter,
                                            topic = (item["topic"] as? String) ?: "General",
                                            difficulty = (item["difficulty"] as? String) ?: "Medium",
                                            text = qText,
                                            options = opts,
                                            correctIndex = corrIdx,
                                            explanation = (item["explanation"] as? String) ?: "",
                                            order = idx + 1
                                        )
                                    )
                                }
                            }
                        }
                        if (parsedQuestions.isNotEmpty()) {
                            questionsMap["chapter_doc_array"] = parsedQuestions
                            emitCombined()
                        }
                    }
                }
            }
            listeners.add(lChapterDoc)

            // Listen to all Topics under this chapter
            val lTopicsSub = chapterDocRef.collection("Topics").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    snapshot.documents.forEach { topicDoc ->
                        val topicName = topicDoc.getString("name") ?: topicDoc.id
                        topicDoc.reference.collection("Questions").addSnapshotListener { qSnap, qErr ->
                            if (qErr == null && qSnap != null && !qSnap.isEmpty) {
                                val list = qSnap.documents.mapIndexedNotNull { index, doc ->
                                    parseFirestoreQuestion(doc, test.id, test.exam, index)
                                }
                                questionsMap["topic_${topicDoc.id}"] = list
                                emitCombined()
                            }
                        }
                    }
                }
            }
            listeners.add(lTopicsSub)

            // Lowercase exams/subjects/chapters path fallback
            val lExamsLowerQuestions = db.collection("exams").document(examKey)
                .collection("subjects").document(subject.lowercase())
                .collection("chapters").document(chapter)
                .collection("Questions")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapIndexedNotNull { index, doc ->
                            parseFirestoreQuestion(doc, test.id, test.exam, index)
                        }
                        questionsMap["exams_lower_questions"] = list
                        emitCombined()
                    }
                }
            listeners.add(lExamsLowerQuestions)
        }

        // 2. Filter at the Firestore query level using testId
        if (test.id.isNotBlank()) {
            val lQuestionsByTestId = db.collection("questions")
                .whereEqualTo("testId", test.id)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapIndexedNotNull { index, doc ->
                            parseFirestoreQuestion(doc, test.id, test.exam, index)
                        }
                        questionsMap["root_questions_by_testid"] = list
                        emitCombined()
                    }
                }
            listeners.add(lQuestionsByTestId)

            val lQuestionsUpperByTestId = db.collection("Questions")
                .whereEqualTo("testId", test.id)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapIndexedNotNull { index, doc ->
                            parseFirestoreQuestion(doc, test.id, test.exam, index)
                        }
                        questionsMap["root_questions_upper_by_testid"] = list
                        emitCombined()
                    }
                }
            listeners.add(lQuestionsUpperByTestId)

            val lSubQuestions = db.collection("tests").document(test.id)
                .collection("questions")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapIndexedNotNull { index, doc ->
                            parseFirestoreQuestion(doc, test.id, test.exam, index)
                        }
                        questionsMap["sub_questions_by_testid"] = list
                        emitCombined()
                    }
                }
            listeners.add(lSubQuestions)
        }

        awaitClose {
            listeners.forEach { it.remove() }
        }
    }

    /**
     * Loads questions for a historical attempt by querying Firestore using attempt.answers keys
     * or attempt.testId across all standard question collections.
     */
    suspend fun getQuestionsForAttempt(attempt: Attempt): List<Question> {
        val resultList = mutableListOf<Question>()
        val seenIds = mutableSetOf<String>()

        fun addQuestions(docs: List<com.google.firebase.firestore.DocumentSnapshot>) {
            docs.forEachIndexed { idx, doc ->
                if (!seenIds.contains(doc.id)) {
                    val q = parseFirestoreQuestion(doc, attempt.testId, attempt.exam, idx)
                    if (q != null) {
                        seenIds.add(doc.id)
                        resultList.add(q)
                    }
                }
            }
        }

        // 1. Try querying by question IDs in attempt.answers.keys
        val answerQIds = attempt.answers.keys.filter { it.isNotBlank() }
        if (answerQIds.isNotEmpty()) {
            for (chunk in answerQIds.chunked(10)) {
                try {
                    val rootSnap = db.collection("questions")
                        .whereIn(FieldPath.documentId(), chunk)
                        .get().await()
                    addQuestions(rootSnap.documents)
                } catch (e: Exception) {
                    Log.w("MarksRepo", "Error fetching questions by IDs in root questions: ${e.message}")
                }

                try {
                    val rootCapSnap = db.collection("Questions")
                        .whereIn(FieldPath.documentId(), chunk)
                        .get().await()
                    addQuestions(rootCapSnap.documents)
                } catch (e: Exception) {
                    Log.w("MarksRepo", "Error fetching questions by IDs in root Questions: ${e.message}")
                }
            }

            // Direct document lookups for any IDs not resolved by whereIn
            if (resultList.size < answerQIds.size) {
                for (qId in answerQIds) {
                    if (!seenIds.contains(qId)) {
                        try {
                            val directDoc = db.collection("questions").document(qId).get().await()
                            if (directDoc.exists()) {
                                addQuestions(listOf(directDoc))
                            }
                        } catch (_: Exception) {}
                        if (!seenIds.contains(qId)) {
                            try {
                                val directDocUpper = db.collection("Questions").document(qId).get().await()
                                if (directDocUpper.exists()) {
                                    addQuestions(listOf(directDocUpper))
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
        }

        // 2. Query by attempt.testId if available
        if (attempt.testId.isNotBlank()) {
            // A. Root 'questions' by testId
            try {
                val qByTestId = db.collection("questions")
                    .whereEqualTo("testId", attempt.testId)
                    .get().await()
                addQuestions(qByTestId.documents)
            } catch (e: Exception) {
                Log.w("MarksRepo", "Error fetching by testId in questions: ${e.message}")
            }

            try {
                val qByTestIdUpper = db.collection("Questions")
                    .whereEqualTo("testId", attempt.testId)
                    .get().await()
                addQuestions(qByTestIdUpper.documents)
            } catch (e: Exception) {
                Log.w("MarksRepo", "Error fetching by testId in Questions: ${e.message}")
            }

            // B. Subcollection tests/{testId}/questions or Tests/{testId}/Questions
            try {
                val subQuestions = db.collection("tests").document(attempt.testId)
                    .collection("questions").get().await()
                addQuestions(subQuestions.documents)
            } catch (_: Exception) {}

            try {
                val subQuestionsUpper = db.collection("Tests").document(attempt.testId)
                    .collection("Questions").get().await()
                addQuestions(subQuestionsUpper.documents)
            } catch (_: Exception) {}

            // C. Embedded questions in the test document itself
            try {
                val testDoc = db.collection("tests").document(attempt.testId).get().await()
                if (testDoc.exists()) {
                    val embedded = parseEmbeddedQuestions(testDoc, attempt.testId, attempt.exam)
                    embedded.forEach { if (!seenIds.contains(it.id)) { seenIds.add(it.id); resultList.add(it) } }
                }
            } catch (_: Exception) {}

            try {
                val testDocUpper = db.collection("Tests").document(attempt.testId).get().await()
                if (testDocUpper.exists()) {
                    val embedded = parseEmbeddedQuestions(testDocUpper, attempt.testId, attempt.exam)
                    embedded.forEach { if (!seenIds.contains(it.id)) { seenIds.add(it.id); resultList.add(it) } }
                }
            } catch (_: Exception) {}

            // D. Collection group Queries
            try {
                val groupSnap = db.collectionGroup("Questions")
                    .whereEqualTo("testId", attempt.testId)
                    .get().await()
                addQuestions(groupSnap.documents)
            } catch (e: Exception) {
                Log.w("MarksRepo", "Error fetching collectionGroup Questions by testId: ${e.message}")
            }

            try {
                val groupSnapLower = db.collectionGroup("questions")
                    .whereEqualTo("testId", attempt.testId)
                    .get().await()
                addQuestions(groupSnapLower.documents)
            } catch (e: Exception) {
                Log.w("MarksRepo", "Error fetching collectionGroup questions by testId: ${e.message}")
            }
        }

        // Sort by answer sequence if answers map provided, or by question order
        if (answerQIds.isNotEmpty()) {
            val idOrderMap = answerQIds.mapIndexed { idx, id -> id to idx }.toMap()
            resultList.sortBy { idOrderMap[it.id] ?: (it.order + 1000) }
        } else {
            resultList.sortBy { it.order }
        }

        return resultList
    }

    private fun parseEmbeddedQuestions(
        docSnap: com.google.firebase.firestore.DocumentSnapshot,
        testId: String,
        exam: String
    ): List<Question> {
        val qListRaw = docSnap.get("questions") as? List<*> ?: return emptyList()
        val parsedQuestions = mutableListOf<Question>()
        qListRaw.forEachIndexed { idx, item ->
            if (item is Map<*, *>) {
                val qText = (item["text"] as? String) ?: (item["question"] as? String) ?: ""
                val optsRaw = item["options"] as? List<*>
                val opts = optsRaw?.map { it?.toString() ?: "" } ?: emptyList()
                val corrRaw = item["correctIndex"] ?: item["answer"] ?: item["correct"] ?: 0
                val corrIdx = when (corrRaw) {
                    is Number -> corrRaw.toInt().let { if (it in 1..4) it - 1 else it.coerceIn(0, 3) }
                    is String -> when (corrRaw.trim().uppercase()) {
                        "B", "2" -> 1
                        "C", "3" -> 2
                        "D", "4" -> 3
                        else -> 0
                    }
                    else -> 0
                }
                if (qText.isNotBlank()) {
                    parsedQuestions.add(
                        Question(
                            id = (item["id"] as? String) ?: "${docSnap.id}_q_$idx",
                            testId = testId,
                            exam = exam,
                            text = qText,
                            options = opts,
                            correctIndex = corrIdx,
                            explanation = (item["explanation"] as? String) ?: "",
                            order = idx + 1
                        )
                    )
                }
            }
        }
        return parsedQuestions
    }

    private fun loadQuestionsFromAllTopics(
        examKey: String,
        subject: String,
        chapter: String,
        test: Test,
        onResult: (List<Question>) -> Unit
    ) {
        if (subject.isNotBlank() && chapter.isNotBlank()) {
            db.collection("Exams").document(examKey)
                .collection("Subjects").document(subject)
                .collection("Chapters").document(chapter)
                .collection("Topics").get()
                .addOnSuccessListener { topicsSnap ->
                    if (topicsSnap.isEmpty) {
                        queryRootQuestions(test, onResult)
                        return@addOnSuccessListener
                    }
                    val allQuestions = mutableListOf<Question>()
                    var pending = topicsSnap.size()
                    for (topicDoc in topicsSnap.documents) {
                        topicDoc.reference.collection("Questions").get()
                            .addOnSuccessListener { qSnap ->
                                qSnap.documents.forEachIndexed { idx, doc ->
                                    parseFirestoreQuestion(doc, test.id, test.exam, allQuestions.size + idx)?.let {
                                        allQuestions.add(it)
                                    }
                                }
                                pending--
                                if (pending <= 0) {
                                    if (allQuestions.isNotEmpty()) {
                                        onResult(allQuestions)
                                    } else {
                                        queryRootQuestions(test, onResult)
                                    }
                                }
                            }
                            .addOnFailureListener {
                                pending--
                                if (pending <= 0) {
                                    if (allQuestions.isNotEmpty()) {
                                        onResult(allQuestions)
                                    } else {
                                        queryRootQuestions(test, onResult)
                                    }
                                }
                            }
                    }
                }
                .addOnFailureListener {
                    queryRootQuestions(test, onResult)
                }
        } else {
            queryRootQuestions(test, onResult)
        }
    }

    private fun queryRootQuestions(test: Test, onResult: (List<Question>) -> Unit) {
        val chapterName = test.chapter.ifBlank { test.title }
        db.collectionGroup("Questions")
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.filter { matchesChapter(it, test) }.mapIndexedNotNull { index, doc ->
                    parseFirestoreQuestion(doc, test.id, test.exam, index)
                }
                if (list.isNotEmpty()) {
                    onResult(list)
                } else {
                    db.collection("questions")
                        .get()
                        .addOnSuccessListener { chSnap ->
                            val chList = chSnap.documents.filter { matchesChapter(it, test) }.mapIndexedNotNull { index, doc ->
                                parseFirestoreQuestion(doc, test.id, test.exam, index)
                            }
                            onResult(chList)
                        }
                        .addOnFailureListener {
                            onResult(emptyList())
                        }
                }
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    private fun parseFirestoreQuestion(
        doc: com.google.firebase.firestore.DocumentSnapshot,
        testId: String,
        exam: String,
        orderIndex: Int
    ): Question? {
        try {
            // 1. Question text and diagram images
            val qMap = doc.get("question") as? Map<*, *>
            val rawQText = (qMap?.get("text") as? String)
                ?: (doc.get("question") as? String)
                ?: doc.getString("text")
                ?: doc.getString("questionText")
                ?: doc.getString("question_text")
                ?: doc.getString("statement")
                ?: doc.getString("title")
                ?: doc.getString("qText")
                ?: doc.getString("q_text")
                ?: doc.getString("prompt")
                ?: ""
            var qImg = (qMap?.get("image") as? String)
                ?: doc.getString("imageUrl")
                ?: doc.getString("image")
                ?: doc.getString("question_image")
                ?: doc.getString("img")
                ?: ""
            val allQImgs = mutableListOf<String>()
            if (qImg.isNotBlank()) allQImgs.add(qImg)
            extractAllImgUrls(rawQText).forEach { if (!allQImgs.contains(it)) allQImgs.add(it) }
            if (qImg.isBlank() && allQImgs.isNotEmpty()) {
                qImg = allQImgs.first()
            }
            val qText = com.example.util.MathFormatter.formatMath(cleanHtmlText(rawQText))

            if (qText.isBlank() && allQImgs.isEmpty()) return null

            // 2. Options (A, B, C, D) supporting maps, lists, and direct root fields
            val optionsMap = doc.get("options") as? Map<*, *>
            val listRaw = doc.get("options") as? List<*>
            val optionsList = mutableListOf<String>()
            val optionImagesList = mutableListOf<String>()
            var optionFlaggedIndex = -1

            if (optionsMap != null) {
                listOf("A", "B", "C", "D").forEachIndexed { index, optKey ->
                    val optObj = optionsMap[optKey] ?: optionsMap[optKey.lowercase()] ?: optionsMap["opt$optKey"] ?: optionsMap["opt_${optKey.lowercase()}"]
                        ?: optionsMap["${index + 1}"] ?: optionsMap["$index"]
                    var optText = ""
                    var optImg = ""
                    if (optObj is Map<*, *>) {
                        val rawText = (optObj["text"] as? String) ?: (optObj["title"] as? String) ?: ""
                        optImg = (optObj["image"] as? String) ?: ""
                        if (optImg.isBlank()) {
                            optImg = extractImgUrl(rawText)
                        }
                        optText = cleanHtmlText(rawText)
                        val isCorr = optObj["isCorrect"] == true || optObj["is_correct"] == true || optObj["correct"] == true
                        if (isCorr) optionFlaggedIndex = index
                    } else if (optObj is String) {
                        optImg = extractImgUrl(optObj)
                        optText = cleanHtmlText(optObj)
                    }
                    optionsList.add(com.example.util.MathFormatter.formatMath(optText))
                    optionImagesList.add(optImg)
                }
            } else if (listRaw != null) {
                listRaw.forEachIndexed { index, item ->
                    if (item is Map<*, *>) {
                        val raw = (item["text"] as? String) ?: (item["title"] as? String) ?: ""
                        val img = (item["image"] as? String) ?: extractImgUrl(raw)
                        val txt = cleanHtmlText(raw)
                        optionsList.add(com.example.util.MathFormatter.formatMath(txt))
                        optionImagesList.add(img)
                        val isCorr = item["isCorrect"] == true || item["is_correct"] == true || item["correct"] == true
                        if (isCorr && index in 0..3) optionFlaggedIndex = index
                    } else {
                        val raw = item?.toString() ?: ""
                        val img = extractImgUrl(raw)
                        val txt = cleanHtmlText(raw)
                        optionsList.add(com.example.util.MathFormatter.formatMath(txt))
                        optionImagesList.add(img)
                    }
                }
            } else {
                listOf("A", "B", "C", "D").forEachIndexed { index, optKey ->
                    val optVal = doc.get("option$optKey")
                        ?: doc.get("option_${optKey.lowercase()}")
                        ?: doc.get("option${optKey.lowercase()}")
                        ?: doc.get("opt$optKey")
                        ?: doc.get("opt${optKey.lowercase()}")
                        ?: doc.get("opt${index + 1}")
                        ?: doc.get("option${index + 1}")
                        ?: doc.get(optKey.lowercase())
                        ?: doc.get(optKey)
                        ?: ""
                    var optText = ""
                    var optImg = ""
                    if (optVal is Map<*, *>) {
                        optText = (optVal["text"] as? String) ?: ""
                        optImg = (optVal["image"] as? String) ?: ""
                        val isCorr = optVal["isCorrect"] == true || optVal["is_correct"] == true || optVal["correct"] == true
                        if (isCorr) optionFlaggedIndex = index
                    } else {
                        optText = optVal.toString()
                    }
                    optionsList.add(com.example.util.MathFormatter.formatMath(cleanHtmlText(optText)))
                    optionImagesList.add(optImg)
                }
            }

            while (optionsList.size < 4) {
                optionsList.add("")
                optionImagesList.add("")
            }

            // 3. Solution / Explanation
            val solMap = doc.get("solution") as? Map<*, *>
            val rawSolText = (solMap?.get("text") as? String)
                ?: (doc.get("solution") as? String)
                ?: doc.getString("explanation")
                ?: doc.getString("solution_text")
                ?: doc.getString("solutionText")
                ?: doc.getString("hint")
                ?: doc.getString("sol")
                ?: doc.getString("description")
                ?: ""
            var solImg = (solMap?.get("image") as? String)
                ?: doc.getString("solutionImageUrl")
                ?: doc.getString("solutionImage")
                ?: doc.getString("solution_image")
                ?: doc.getString("sol_img")
                ?: ""
            val allSolImgs = mutableListOf<String>()
            if (solImg.isNotBlank()) allSolImgs.add(solImg)
            extractAllImgUrls(rawSolText).forEach { if (!allSolImgs.contains(it)) allSolImgs.add(it) }
            if (solImg.isBlank() && allSolImgs.isNotEmpty()) {
                solImg = allSolImgs.first()
            }
            val cleanSolText = com.example.util.MathFormatter.formatMath(cleanHtmlText(rawSolText))
            val videoUrl = doc.getString("video_solution") ?: ""
            val year = doc.getString("year") ?: ""

            // 4. Correct answer index parsing - Comprehensive support for any format/field
            val correctIdxObj = doc.get("correctIndex")
                ?: doc.get("correct_index")
                ?: doc.get("correctOptionIndex")
                ?: doc.get("correct_option_index")
                ?: doc.get("answerIndex")
                ?: doc.get("answer_index")
                ?: doc.get("correct")
                ?: doc.get("correctOption")
                ?: doc.get("correct_option")
                ?: doc.get("correctOpt")
                ?: doc.get("correct_opt")
                ?: doc.get("correctAnswer")
                ?: doc.get("correct_answer")
                ?: doc.get("correctAns")
                ?: doc.get("correct_ans")
                ?: doc.get("answer")
                ?: doc.get("ans")
                ?: doc.get("answer_key")
                ?: doc.get("answerKey")
                ?: doc.get("key")
                ?: doc.get("rightAnswer")
                ?: doc.get("right_answer")
                ?: doc.get("rightOption")
                ?: doc.get("right_option")
                ?: (solMap?.get("correctIndex") ?: solMap?.get("correct_index") ?: solMap?.get("correct")
                    ?: solMap?.get("correctOption") ?: solMap?.get("correct_option") ?: solMap?.get("answer")
                    ?: solMap?.get("ans") ?: solMap?.get("key"))
                ?: (qMap?.get("correctIndex") ?: qMap?.get("correct") ?: qMap?.get("correctOption") ?: qMap?.get("answer"))

            val correctIdx = if (optionFlaggedIndex in 0..3) {
                optionFlaggedIndex
            } else when (correctIdxObj) {
                is Number -> {
                    val n = correctIdxObj.toInt()
                    // If 1..4, it is 1-based (Option 1 = 0, Option 2 = 1, Option 3 = 2, Option 4 = 3)
                    if (n in 1..4) {
                        n - 1
                    } else {
                        n.coerceIn(0, 3)
                    }
                }
                is String -> {
                    val rawStr = correctIdxObj.trim()
                    val cleaned = rawStr.uppercase()
                        .replace("(", "").replace(")", "").replace("[", "").replace("]", "")
                        .replace(".", "").replace(":", "").replace("-", "")
                        .replace("OPTION", "").replace("OPT", "").replace("CHOICE", "")
                        .replace("ANSWER", "").replace("ANS", "")
                        .trim()

                    when (cleaned) {
                        "A", "1" -> 0
                        "B", "2" -> 1
                        "C", "3" -> 2
                        "D", "4" -> 3
                        "0" -> 0
                        else -> {
                            val normRaw = cleanHtmlText(rawStr).trim().lowercase()
                            val textMatch = optionsList.indexOfFirst { opt ->
                                val normOpt = cleanHtmlText(opt).trim().lowercase()
                                normOpt.isNotBlank() && (normOpt == normRaw || normOpt.contains(normRaw) || normRaw.contains(normOpt))
                            }
                            if (textMatch in 0..3) {
                                textMatch
                            } else if (cleaned.toIntOrNull() != null) {
                                val intVal = cleaned.toInt()
                                if (intVal in 1..4) (intVal - 1) else intVal.coerceIn(0, 3)
                            } else {
                                val solMatch = Regex("""(?i)(?:correct\s*(?:option|answer)|ans(?:wer)?|option)\s*(?:is|:|\=|\-)?\s*[\(\[]?\s*([A-D]|[1-4])\s*[\)\]\.]?""")
                                    .find(rawSolText)
                                if (solMatch != null) {
                                    when (solMatch.groupValues[1].uppercase()) {
                                        "A", "1" -> 0
                                        "B", "2" -> 1
                                        "C", "3" -> 2
                                        "D", "4" -> 3
                                        else -> 0
                                    }
                                } else 0
                            }
                        }
                    }
                }
                else -> {
                    val solMatch = Regex("""(?i)(?:correct\s*(?:option|answer)|ans(?:wer)?|option)\s*(?:is|:|\=|\-)?\s*[\(\[]?\s*([A-D]|[1-4])\s*[\)\]\.]?""")
                        .find(rawSolText)
                    if (solMatch != null) {
                        when (solMatch.groupValues[1].uppercase()) {
                            "A", "1" -> 0
                            "B", "2" -> 1
                            "C", "3" -> 2
                            "D", "4" -> 3
                            else -> 0
                        }
                    } else 0
                }
            }

            val docExam = doc.getString("exam") ?: doc.getString("branch") ?: exam
            val rawSubject = doc.getString("subject") ?: doc.getString("category") ?: doc.getString("sub") ?: doc.getString("categoryId") ?: ""
            val normSubject = when {
                rawSubject.contains("botan", true) -> "Botany"
                rawSubject.contains("zoo", true) -> "Zoology"
                rawSubject.contains("chem", true) -> "Chemistry"
                rawSubject.contains("phys", true) -> "Physics"
                rawSubject.contains("math", true) -> "Mathematics"
                rawSubject.contains("bio", true) -> if (docExam.contains("neet", true)) "Botany" else "Biology"
                else -> rawSubject.ifBlank { if (docExam.contains("neet", true)) "Botany" else "Physics" }
            }
            val rawChapter = doc.getString("chapter") ?: doc.getString("chapter_name") ?: doc.getString("chapterName") ?: doc.getString("unit") ?: ""

            val parentColId = doc.reference.parent.id
            val grandparentDocId = doc.reference.parent.parent?.id
            val greatGrandparentColId = doc.reference.parent.parent?.parent?.id
            val greatGrandparentDocId = doc.reference.parent.parent?.parent?.parent?.id

            // Check if difficulty is encoded in path (.../Difficulties/{difficulty}/Questions)
            val pathDiff = when {
                greatGrandparentColId == "Difficulties" -> grandparentDocId
                grandparentDocId in listOf("Easy", "Medium", "Hard", "Moderate", "Tough") -> grandparentDocId
                else -> null
            }

            // Check if topic is encoded in path (.../Topics/{topic}/...)
            val pathTopic = when {
                greatGrandparentColId == "Topics" -> greatGrandparentDocId
                grandparentDocId != null && grandparentDocId !in listOf("Easy", "Medium", "Hard", "Moderate", "Tough", "General") -> grandparentDocId
                else -> null
            }

            val rawTopic = doc.getString("topic")
                ?: doc.getString("topic_name")
                ?: doc.getString("topicName")
                ?: doc.getString("subtopic")
                ?: pathTopic
                ?: "General"

            val rawDifficulty = doc.getString("difficulty")
                ?: doc.getString("level")
                ?: doc.getString("difficulty_level")
                ?: doc.getString("diff")
                ?: pathDiff
                ?: "Medium"

            val normDifficulty = when (rawDifficulty.trim().lowercase()) {
                "easy", "beginner", "simple", "basic" -> "Easy"
                "hard", "tough", "advanced", "difficult", "expert" -> "Hard"
                else -> "Medium"
            }

            return Question(
                id = doc.id,
                testId = testId,
                exam = docExam,
                subject = normSubject,
                chapter = rawChapter,
                topic = rawTopic,
                difficulty = normDifficulty,
                classLevel = doc.getString("classLevel") ?: "12th",
                text = qText,
                options = optionsList,
                optionImages = optionImagesList,
                correctIndex = correctIdx,
                explanation = cleanSolText,
                solutionImageUrl = solImg,
                solutionImageUrls = allSolImgs,
                videoSolution = videoUrl,
                year = year,
                marks = (doc.get("marks") as? Number)?.toDouble() ?: 4.0,
                imageUrl = qImg,
                imageUrls = allQImgs,
                order = orderIndex + 1
            )
        } catch (e: Exception) {
            Log.w("MarksRepo", "Error parsing question ${doc.id}: ${e.message}")
            return null
        }
    }

    suspend fun saveAttempt(
        testId: String,
        testTitle: String,
        exam: String,
        score: Double,
        totalMarks: Double,
        correctCount: Int,
        wrongCount: Int,
        skippedCount: Int,
        timeTakenSeconds: Int,
        answers: Map<String, Int>
    ): String {
        val user = auth.currentUser
        val attemptId = UUID.randomUUID().toString()

        val attempt = Attempt(
            id = attemptId,
            userId = user?.uid ?: "guest",
            testId = testId,
            testTitle = testTitle,
            exam = exam,
            score = score,
            totalMarks = totalMarks,
            correctCount = correctCount,
            wrongCount = wrongCount,
            skippedCount = skippedCount,
            timeTakenSeconds = timeTakenSeconds,
            answers = answers,
            completed = true
        )

        // Store in guest memory if unauthenticated
        val updated = listOf(attempt) + guestAttempts.value
        guestAttempts.value = updated

        if (user != null) {
            try {
                val payload = hashMapOf<String, Any>(
                    "userId" to user.uid,
                    "testId" to testId,
                    "testTitle" to testTitle,
                    "exam" to exam,
                    "score" to score,
                    "totalMarks" to totalMarks,
                    "correctCount" to correctCount,
                    "wrongCount" to wrongCount,
                    "skippedCount" to skippedCount,
                    "timeTakenSeconds" to timeTakenSeconds,
                    "answers" to answers,
                    "completed" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(user.uid).collection("attempts").add(payload).await()
            } catch (e: Exception) {
                Log.w("MarksRepo", "Error saving attempt to Firestore: ${e.message}")
            }
        }

        return attemptId
    }

    fun observeUserAttempts(): Flow<List<Attempt>> {
        val user = auth.currentUser ?: return guestAttempts

        return callbackFlow {
            trySend(guestAttempts.value)
            val listener = db.collection("users").document(user.uid).collection("attempts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(guestAttempts.value)
                        return@addSnapshotListener
                    }
                    val remote = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Attempt::class.java)?.copy(id = doc.id)
                    } ?: emptyList()
                    trySend(remote.ifEmpty { guestAttempts.value })
                }

            awaitClose { listener.remove() }
        }
    }

    fun observeUserBookmarks(): Flow<List<Bookmark>> {
        val user = auth.currentUser ?: return guestBookmarks

        return callbackFlow {
            trySend(guestBookmarks.value)
            val listener = db.collection("users").document(user.uid).collection("bookmarks")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(guestBookmarks.value)
                        return@addSnapshotListener
                    }
                    val remote = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Bookmark::class.java)?.copy(id = doc.id)
                    } ?: emptyList()
                    trySend(remote.ifEmpty { guestBookmarks.value })
                }

            awaitClose { listener.remove() }
        }
    }

    suspend fun toggleBookmark(questionId: String, testId: String, exam: String, questionText: String): Boolean {
        val user = auth.currentUser
        val currentList = guestBookmarks.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.questionId == questionId }

        val isBookmarked: Boolean
        if (existingIndex >= 0) {
            currentList.removeAt(existingIndex)
            isBookmarked = false
        } else {
            currentList.add(0, Bookmark(id = UUID.randomUUID().toString(), questionId = questionId, testId = testId, exam = exam, questionText = questionText))
            isBookmarked = true
        }
        guestBookmarks.value = currentList

        if (user != null) {
            try {
                val ref = db.collection("users").document(user.uid).collection("bookmarks")
                val existing = ref.whereEqualTo("questionId", questionId).get().await()
                if (!existing.isEmpty) {
                    for (doc in existing.documents) {
                        doc.reference.delete().await()
                    }
                } else {
                    val payload = hashMapOf<String, Any>(
                        "userId" to user.uid,
                        "questionId" to questionId,
                        "testId" to testId,
                        "exam" to exam,
                        "questionText" to questionText,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                    ref.add(payload).await()
                }
            } catch (e: Exception) {
                Log.w("MarksRepo", "Error updating bookmark in Firestore: ${e.message}")
            }
        }

        return isBookmarked
    }
}
