package com.cricut.androidassessment.data.repository

import com.cricut.androidassessment.data.model.Question
import kotlinx.coroutines.flow.Flow

interface QuizRepository {
    fun getQuizQuestions(): Flow<List<Question>>
}