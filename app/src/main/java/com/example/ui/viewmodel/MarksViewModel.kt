package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Attempt
import com.example.data.model.Bookmark
import com.example.data.model.Category
import com.example.data.model.ExamBranch
import com.example.data.model.Question
import com.example.data.model.QuestionPaletteStatus
import com.example.data.model.Test
import com.example.data.repository.MarksRepository
import com.example.util.UserSessionManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class ActiveTestSession(
    val test: Test,
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val answers: Map<String, Int> = emptyMap(),
    val statusMap: Map<String, QuestionPaletteStatus> = emptyMap(),
    val timeRemainingSeconds: Int = 0,
    val totalTimeSeconds: Int = 0,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val submittedAttempt: Attempt? = null,
    val selectedTopic: String? = null,
    val selectedDifficulty: String? = null,
    val isLoading: Boolean = true
)

class MarksViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MarksRepository(application)

    // Dynamic Exam Branches (JEE, NEET, and any future exams from Firestore)
    val examBranchesState: StateFlow<List<ExamBranch>> = repository.observeExamBranches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(
                ExamBranch(id = "jee", name = "JEE", displayName = "JEE Main & Adv", order = 1),
                ExamBranch(id = "neet", name = "NEET", displayName = "NEET Medical", order = 2)
            )
        )

    private val _targetStream = MutableStateFlow<String>(
        UserSessionManager.getTargetStream(application)
    )
    val targetStream: StateFlow<String> = _targetStream.asStateFlow()

    private val _selectedExamBranch = MutableStateFlow<String>(
        if (UserSessionManager.getTargetStream(application).equals("Medical", ignoreCase = true)) "NEET" else "JEE Main"
    )
    val selectedExamBranch: StateFlow<String> = _selectedExamBranch.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    // Real-time categories for selected exam branch
    private val _categoriesState = MutableStateFlow<UiState<List<Category>>>(UiState.Loading)
    val categoriesState: StateFlow<UiState<List<Category>>> = _categoriesState.asStateFlow()

    // Real-time tests for selected exam branch & category
    private val _testsState = MutableStateFlow<UiState<List<Test>>>(UiState.Loading)
    val testsState: StateFlow<UiState<List<Test>>> = _testsState.asStateFlow()

    val userAttemptsState: StateFlow<UiState<List<Attempt>>> = repository.observeUserAttempts()
        .map<List<Attempt>, UiState<List<Attempt>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load test attempts")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    val userBookmarksState: StateFlow<UiState<List<Bookmark>>> = repository.observeUserBookmarks()
        .map<List<Bookmark>, UiState<List<Bookmark>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load bookmarks")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    private val _activeSession = MutableStateFlow<ActiveTestSession?>(null)
    val activeSession: StateFlow<ActiveTestSession?> = _activeSession.asStateFlow()

    private var timerJob: Job? = null
    private var categoriesJob: Job? = null
    private var testsJob: Job? = null

    init {
        observeCategoriesForBranch(_selectedExamBranch.value)
        observeTestsForCurrentSelection()
    }

    fun onUserAuthenticated() {
        // Real-time online user session synced
    }

    fun setTargetStream(stream: String) {
        _targetStream.value = stream
        UserSessionManager.setTargetStream(getApplication(), stream)
        val defaultBranch = if (stream.equals("Medical", ignoreCase = true)) "NEET" else "JEE Main"
        selectExamBranch(defaultBranch)
    }

    fun selectExamBranch(branchName: String) {
        _selectedExamBranch.value = branchName
        _selectedCategoryId.value = null
        observeCategoriesForBranch(branchName)
        observeTestsForCurrentSelection()
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
        observeTestsForCurrentSelection()
    }

    private fun observeCategoriesForBranch(branch: String) {
        categoriesJob?.cancel()
        _categoriesState.value = UiState.Loading
        categoriesJob = viewModelScope.launch {
            repository.observeCategories(branch)
                .catch { e -> _categoriesState.value = UiState.Error(e.message ?: "Error loading categories") }
                .collect { list ->
                    _categoriesState.value = UiState.Success(list)
                }
        }
    }

    private fun observeTestsForCurrentSelection() {
        testsJob?.cancel()
        _testsState.value = UiState.Loading
        testsJob = viewModelScope.launch {
            repository.observeTests(_selectedExamBranch.value, _selectedCategoryId.value)
                .catch { e -> _testsState.value = UiState.Error(e.message ?: "Error loading tests") }
                .collect { list ->
                    _testsState.value = UiState.Success(list)
                }
        }
    }

    fun refreshData() {
        observeCategoriesForBranch(_selectedExamBranch.value)
        observeTestsForCurrentSelection()
    }

    fun saveAttempt(attempt: Attempt) {
        viewModelScope.launch {
            try {
                repository.saveAttempt(
                    testId = attempt.testId,
                    testTitle = attempt.testTitle,
                    exam = attempt.exam,
                    score = attempt.score,
                    totalMarks = attempt.totalMarks,
                    correctCount = attempt.correctCount,
                    wrongCount = attempt.wrongCount,
                    skippedCount = attempt.skippedCount,
                    timeTakenSeconds = attempt.timeTakenSeconds,
                    answers = attempt.answers
                )
            } catch (e: Exception) {
                Log.e("MarksVM", "Failed to save attempt", e)
            }
        }
    }

    fun getTopicsForChapter(test: Test): kotlinx.coroutines.flow.Flow<List<String>> {
        return repository.observeTopicsForChapter(test)
    }

    fun startTest(test: Test, topic: String? = null, difficulty: String? = null) {
        viewModelScope.launch {
            _activeSession.value = ActiveTestSession(
                test = test,
                timeRemainingSeconds = test.duration * 60,
                totalTimeSeconds = test.duration * 60,
                selectedTopic = topic,
                selectedDifficulty = difficulty,
                isLoading = true
            )

            repository.observeQuestions(test, topic, difficulty).collect { questions ->
                val current = _activeSession.value
                if (current != null && current.test.id == test.id) {
                    val initialStatus = current.statusMap.toMutableMap()
                    questions.forEach { q ->
                        if (!initialStatus.containsKey(q.id)) {
                            initialStatus[q.id] = QuestionPaletteStatus.NOT_VISITED
                        }
                    }

                    if (questions.isNotEmpty()) {
                        val firstId = questions[0].id
                        if (initialStatus[firstId] == null || initialStatus[firstId] == QuestionPaletteStatus.NOT_VISITED) {
                            initialStatus[firstId] = QuestionPaletteStatus.NOT_ANSWERED
                        }
                    }

                    _activeSession.value = current.copy(
                        questions = questions,
                        statusMap = initialStatus,
                        isLoading = false
                    )
                    startTimer()
                }
            }
        }
    }

    fun reloadQuestionsWithoutFilter() {
        val current = _activeSession.value ?: return
        startTest(current.test, topic = null, difficulty = null)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _activeSession.value ?: break
                if (current.isSubmitted) break
                val remaining = current.timeRemainingSeconds - 1
                if (remaining <= 0) {
                    _activeSession.value = current.copy(timeRemainingSeconds = 0)
                    submitActiveTest(isAutoSubmit = true)
                    break
                } else {
                    _activeSession.value = current.copy(timeRemainingSeconds = remaining)
                }
            }
        }
    }

    fun selectAnswer(optionIndex: Int) {
        val current = _activeSession.value ?: return
        val currentQuestion = current.questions.getOrNull(current.currentQuestionIndex) ?: return

        val newAnswers = current.answers.toMutableMap()
        newAnswers[currentQuestion.id] = optionIndex

        val newStatus = current.statusMap.toMutableMap()
        val currentStatus = newStatus[currentQuestion.id]
        newStatus[currentQuestion.id] = if (currentStatus == QuestionPaletteStatus.MARKED_FOR_REVIEW || currentStatus == QuestionPaletteStatus.ANSWERED_AND_MARKED) {
            QuestionPaletteStatus.ANSWERED_AND_MARKED
        } else {
            QuestionPaletteStatus.ANSWERED
        }

        _activeSession.value = current.copy(
            answers = newAnswers,
            statusMap = newStatus
        )
    }

    fun clearResponse() {
        val current = _activeSession.value ?: return
        val currentQuestion = current.questions.getOrNull(current.currentQuestionIndex) ?: return

        val newAnswers = current.answers.toMutableMap()
        newAnswers.remove(currentQuestion.id)

        val newStatus = current.statusMap.toMutableMap()
        newStatus[currentQuestion.id] = QuestionPaletteStatus.NOT_ANSWERED

        _activeSession.value = current.copy(
            answers = newAnswers,
            statusMap = newStatus
        )
    }

    fun toggleMarkForReview() {
        val current = _activeSession.value ?: return
        val currentQuestion = current.questions.getOrNull(current.currentQuestionIndex) ?: return

        val hasAnswer = current.answers.containsKey(currentQuestion.id)
        val newStatus = current.statusMap.toMutableMap()
        val currentStatus = newStatus[currentQuestion.id]

        if (currentStatus == QuestionPaletteStatus.MARKED_FOR_REVIEW || currentStatus == QuestionPaletteStatus.ANSWERED_AND_MARKED) {
            newStatus[currentQuestion.id] = if (hasAnswer) QuestionPaletteStatus.ANSWERED else QuestionPaletteStatus.NOT_ANSWERED
        } else {
            newStatus[currentQuestion.id] = if (hasAnswer) QuestionPaletteStatus.ANSWERED_AND_MARKED else QuestionPaletteStatus.MARKED_FOR_REVIEW
        }

        _activeSession.value = current.copy(statusMap = newStatus)
    }

    fun navigateToQuestion(index: Int) {
        val current = _activeSession.value ?: return
        if (index !in current.questions.indices) return

        val targetQuestion = current.questions[index]
        val newStatus = current.statusMap.toMutableMap()
        if (newStatus[targetQuestion.id] == QuestionPaletteStatus.NOT_VISITED) {
            newStatus[targetQuestion.id] = QuestionPaletteStatus.NOT_ANSWERED
        }

        _activeSession.value = current.copy(
            currentQuestionIndex = index,
            statusMap = newStatus
        )
    }

    fun nextQuestion() {
        val current = _activeSession.value ?: return
        if (current.currentQuestionIndex < current.questions.size - 1) {
            navigateToQuestion(current.currentQuestionIndex + 1)
        }
    }

    fun previousQuestion() {
        val current = _activeSession.value ?: return
        if (current.currentQuestionIndex > 0) {
            navigateToQuestion(current.currentQuestionIndex - 1)
        }
    }

    fun submitActiveTest(isAutoSubmit: Boolean = false, onComplete: ((Attempt) -> Unit)? = null) {
        val current = _activeSession.value ?: return
        if (current.isSubmitting || current.isSubmitted) return

        timerJob?.cancel()
        _activeSession.value = current.copy(isSubmitting = true)

        viewModelScope.launch {
            var correct = 0
            var wrong = 0
            var skipped = 0
            var totalScore = 0.0

            for (q in current.questions) {
                val selected = current.answers[q.id]
                if (selected == null) {
                    skipped++
                } else if (selected == q.correctIndex) {
                    correct++
                    totalScore += q.marks
                } else {
                    wrong++
                    totalScore -= current.test.negativeMarking
                }
            }

            if (totalScore < 0) totalScore = 0.0

            val timeSpent = current.totalTimeSeconds - current.timeRemainingSeconds

            try {
                val attemptId = repository.saveAttempt(
                    testId = current.test.id,
                    testTitle = current.test.title,
                    exam = current.test.exam.ifBlank { _selectedExamBranch.value },
                    score = totalScore,
                    totalMarks = current.test.totalMarks,
                    correctCount = correct,
                    wrongCount = wrong,
                    skippedCount = skipped,
                    timeTakenSeconds = timeSpent,
                    answers = current.answers
                )

                val attempt = Attempt(
                    id = attemptId,
                    userId = Firebase.auth.currentUser?.uid ?: "guest",
                    testId = current.test.id,
                    testTitle = current.test.title,
                    exam = current.test.exam.ifBlank { _selectedExamBranch.value },
                    score = totalScore,
                    totalMarks = current.test.totalMarks,
                    correctCount = correct,
                    wrongCount = wrong,
                    skippedCount = skipped,
                    timeTakenSeconds = timeSpent,
                    answers = current.answers,
                    completed = true
                )

                _activeSession.value = (_activeSession.value ?: current).copy(
                    isSubmitting = false,
                    isSubmitted = true,
                    submittedAttempt = attempt
                )
                onComplete?.invoke(attempt)
            } catch (e: Exception) {
                Log.e("MarksViewModel", "Failed to save attempt to Firestore: ${e.message}", e)
                val fallbackAttempt = Attempt(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = Firebase.auth.currentUser?.uid ?: "guest",
                    testId = current.test.id,
                    testTitle = current.test.title,
                    exam = current.test.exam.ifBlank { _selectedExamBranch.value },
                    score = totalScore,
                    totalMarks = current.test.totalMarks,
                    correctCount = correct,
                    wrongCount = wrong,
                    skippedCount = skipped,
                    timeTakenSeconds = timeSpent,
                    answers = current.answers,
                    completed = true
                )
                _activeSession.value = (_activeSession.value ?: current).copy(
                    isSubmitting = false,
                    isSubmitted = true,
                    submittedAttempt = fallbackAttempt
                )
                onComplete?.invoke(fallbackAttempt)
            }
        }
    }

    fun toggleBookmark(question: Question) {
        viewModelScope.launch {
            repository.toggleBookmark(
                questionId = question.id,
                testId = question.testId,
                exam = question.exam.ifBlank { _selectedExamBranch.value },
                questionText = question.text
            )
        }
    }

    fun isBookmarked(questionId: String): Boolean {
        val bookmarks = (userBookmarksState.value as? UiState.Success)?.data ?: return false
        return bookmarks.any { it.questionId == questionId }
    }

    fun resetSession() {
        timerJob?.cancel()
        _activeSession.value = null
    }

    suspend fun getQuestionsForAttempt(attempt: Attempt): List<Question> {
        return repository.getQuestionsForAttempt(attempt)
    }
}
