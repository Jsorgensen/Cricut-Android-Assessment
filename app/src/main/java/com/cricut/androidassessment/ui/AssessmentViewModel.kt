package com.cricut.androidassessment.ui

import androidx.lifecycle.ViewModel
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
                correctAnswer = false,
                points = 10
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
                isLoading = false // Questions are now "loaded"
            )
        }
    }

    fun selectAnswer(questionId: String, answer: Boolean) {
        val currentQuestion = _uiState.value.questions.find { it.id == questionId }
        if (currentQuestion == null || currentQuestion !is TrueFalseQuestion) {
            println("Error: Question not found or not a TrueFalseQuestion for ID: $questionId")
            return
        }

        // Store the user's answer
        _uiState.update { currentState ->
            val newAnswers = currentState.userAnswers.toMutableMap()
            newAnswers[questionId] = answer.toString() // Store boolean as string

            currentState.copy(userAnswers = newAnswers)
        }
    }
}

