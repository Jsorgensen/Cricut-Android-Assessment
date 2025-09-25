package com.cricut.androidassessment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricut.androidassessment.data.model.MultipleChoiceQuestion
import com.cricut.androidassessment.data.model.QuizUiState
import com.cricut.androidassessment.data.model.TrueFalseQuestion
import com.cricut.androidassessment.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AssessmentViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        println("AssessmentViewModel created and injected by Hilt!")
        loadQuestionsFromRepository()
    }

    private fun loadQuestionsFromRepository() {
        _uiState.update { it.copy(isLoading = true) }

        quizRepository.getQuizQuestions()
            .onEach { questions ->
                _uiState.update { currentState ->
                    currentState.copy(
                        questions = questions,
                        isLoading = false,
                        currentQuestionIndex = 0,
                        userAnswers = emptyMap(),
                        isQuizComplete = false,
                        score = 0
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun selectAnswer(questionId: String, answer: String) {
        val currentQuestion = _uiState.value.questions.find { it.id == questionId }
        if (currentQuestion == null) {
            println("Error: Question not found for ID: $questionId")
            return
        }

        if (_uiState.value.currentQuestionIndex < _uiState.value.questions.size - 1) {
            nextQuestion()
        }

        // Store the user's answer
        _uiState.update { currentState ->
            val newAnswers = currentState.userAnswers.toMutableMap()
            newAnswers[questionId] = answer

            currentState.copy(userAnswers = newAnswers)
        }
    }

    fun nextQuestion() {
        _uiState.update { currentState ->
            if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
                currentState.copy(currentQuestionIndex = currentState.currentQuestionIndex + 1)
            } else {
                if (!currentState.isQuizComplete) {
                    calculateScore()
                    currentState.copy(isQuizComplete = true)
                } else {
                    currentState
                }
            }
        }
    }

    private fun calculateScore() {
        var currentScore = 0
        var possibleScore = 0
        val questions = _uiState.value.questions
        val userAnswers = _uiState.value.userAnswers

        questions.forEach { question ->
            possibleScore += question.points
            val userAnswer = userAnswers[question.id]
            if (userAnswer != null) {
                when (question) {
                    is TrueFalseQuestion -> {
                        if (question.correctAnswer.toString() == userAnswer) {
                            currentScore += question.points
                        }
                    }
                    is MultipleChoiceQuestion -> {
                        if (question.correctAnswerOptionId == userAnswer) {
                            currentScore += question.points
                        }
                    }
                }
            }
        }
        _uiState.update { it.copy(
            score = currentScore,
            possibleScore = possibleScore
        ) }
        println("Score calculated! Final Score: $currentScore")
    }

    fun previousQuestion() {
        _uiState.update { currentState ->
            if (currentState.currentQuestionIndex > 0) {
                currentState.copy(
                    currentQuestionIndex = currentState.currentQuestionIndex - 1,
                    isQuizComplete = false
                )
            } else {
                currentState
            }
        }
    }

    fun restartQuiz() {
        _uiState.update { currentState ->
            currentState.copy(
                currentQuestionIndex = 0,
                userAnswers = emptyMap(),
                isQuizComplete = false,
                score = 0,
                isLoading = false
            )
        }

        loadQuestionsFromRepository()

        println("Quiz restarted!")
    }
}

