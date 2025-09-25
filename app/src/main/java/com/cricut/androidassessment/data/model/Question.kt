package com.cricut.androidassessment.data.model

sealed interface Question {
    val id: String
    val text: String
    val points: Int
}
