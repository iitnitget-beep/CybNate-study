package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class ExamBranch(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val displayName: String = "",
    val icon: String = "quiz",
    val order: Int = 0,
    val description: String = ""
)

data class Category(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val exam: String = "",
    val branch: String = "",
    val icon: String = "quiz",
    val order: Int = 0,
    val description: String = "",
    val totalTests: Int = 0
)

data class Test(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val exam: String = "",
    val branch: String = "",
    val examKey: String = "",
    val subject: String = "",
    val chapter: String = "",
    val categoryId: String = "",
    val duration: Int = 30,
    val totalMarks: Double = 100.0,
    val negativeMarking: Double = 1.0,
    val totalQuestions: Int = 25,
    val description: String = "",
    val difficulty: String = "Medium",
    val classLevel: String = "12th",
    val unit: String = "",
    val isRemoved: Boolean = false,
    val questions2026: Int = 10,
    val questions2025: Int = 10
)

data class Question(
    @DocumentId
    val id: String = "",
    val testId: String = "",
    val exam: String = "",
    val branch: String = "",
    val subject: String = "",
    val chapter: String = "",
    val topic: String = "",
    val difficulty: String = "Medium",
    val classLevel: String = "12th",
    val text: String = "",
    val options: List<String> = emptyList(),
    val optionImages: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val explanation: String = "",
    val solutionImageUrl: String = "",
    val videoSolution: String = "",
    val year: String = "",
    val marks: Double = 4.0,
    val imageUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val solutionImageUrls: List<String> = emptyList(),
    val order: Int = 0
)

data class Attempt(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val testId: String = "",
    val testTitle: String = "",
    val exam: String = "",
    val score: Double = 0.0,
    val totalMarks: Double = 0.0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val skippedCount: Int = 0,
    val timeTakenSeconds: Int = 0,
    val answers: Map<String, Int> = emptyMap(),
    val completed: Boolean = true,
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

data class Bookmark(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val questionId: String = "",
    val testId: String = "",
    val exam: String = "",
    val questionText: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

enum class QuestionPaletteStatus {
    NOT_VISITED,
    NOT_ANSWERED,
    ANSWERED,
    MARKED_FOR_REVIEW,
    ANSWERED_AND_MARKED
}
