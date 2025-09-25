package com.cricut.androidassessment.ui

import androidx.lifecycle.ViewModel
import com.cricut.androidassessment.data.model.AnswerOption
import com.cricut.androidassessment.data.model.MultipleChoiceQuestion
import com.cricut.androidassessment.data.model.Question
import com.cricut.androidassessment.data.model.QuizUiState
import com.cricut.androidassessment.data.model.TrueFalseQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AssessmentViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        println("AssessmentViewModel created and injected by Hilt!")
        loadQuestions()
    }

    private fun loadQuestions() {
        // Temporary, hardcoded questions for testing. To be removed later.
        val sampleQuestions: List<Question> = listOf(
            TrueFalseQuestion(
                id = "tf1",
                text = "Kotlin is better than Java.",
                correctAnswer = true,
                points = 10
            ),
            MultipleChoiceQuestion(
                id = "mc1",
                text = "What is the main ingredient in traditional Italian pesto?",
                options = listOf(
                    AnswerOption("opt1_pine", "Pine Nuts"),
                    AnswerOption("opt1_basil", "Basil"),
                    AnswerOption("opt1_garlic", "Garlic"),
                    AnswerOption("opt1_parmesan", "Parmesan Cheese")
                ),
                correctAnswerOptionId = "opt1_basil",
                points = 15
            ),
            TrueFalseQuestion(
                id = "tf2",
                text = "Water boils at 100 degrees Celsius at sea level.",
                correctAnswer = true,
                points = 5
            )
        )
        _uiState.update {
            it.copy(
                questions = sampleQuestions,
                isLoading = false
            )
        }
    }

    fun selectAnswer(questionId: String, answer: String) {
        val currentQuestion = _uiState.value.questions.find { it.id == questionId }
        if (currentQuestion == null) {
            println("Error: Question not found for ID: $questionId")
            return
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
                calculateScore()
                currentState.copy(isQuizComplete = true)
            }
        }
    }

    private fun calculateScore() {
        var currentScore = 0
        val questions = _uiState.value.questions
        val userAnswers = _uiState.value.userAnswers

        questions.forEach { question ->
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
        _uiState.update { it.copy(score = currentScore) }
        println("Quiz finished! Final Score: $currentScore")
    }
}

