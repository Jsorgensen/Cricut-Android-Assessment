package com.cricut.androidassessment.data.model

data class MultipleChoiceQuestion(
    override val id: String,
    override val text: String,
    override val points: Int = 1,
    val options: List<AnswerOption>,
    val correctAnswerOptionId: String
) : Question