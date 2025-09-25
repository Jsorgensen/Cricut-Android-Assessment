package com.cricut.androidassessment.data.model

data class TrueFalseQuestion(
    override val id: String,
    override val text: String,
    override val points: Int = 1,
    val correctAnswer: Boolean
) : Question