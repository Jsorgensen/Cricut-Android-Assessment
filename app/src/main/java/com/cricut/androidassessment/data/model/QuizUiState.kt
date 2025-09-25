package com.cricut.androidassessment.data.model

data class QuizUiState(
    val currentQuestionIndex: Int = 0,
    val questions: List<Question> = emptyList(),
    val userAnswers: Map<String, String> = emptyMap(),
    val isQuizComplete: Boolean = false,
    val isLoading: Boolean = true,
    val score: Int = 0
)