package com.cricut.androidassessment.data.repository

import com.cricut.androidassessment.data.model.AnswerOption
import com.cricut.androidassessment.data.model.MultipleChoiceQuestion
import com.cricut.androidassessment.data.model.Question
import com.cricut.androidassessment.data.model.TrueFalseQuestion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject


class MockQuizRepositoryImpl @Inject constructor() : QuizRepository {

    override fun getQuizQuestions(): Flow<List<Question>> {
        val sampleQuestions: List<Question> = listOf(
            TrueFalseQuestion(
                id = "tf1",
                text = "Kotlin is better than Java.",
                correctAnswer = true,
                points = 10
            ),
            MultipleChoiceQuestion(
                id = "mc1",
                text = "What is the correct way to eat tomato sauce?",
                options = listOf(
                    AnswerOption("opt1_tomato_soup", "Tomato Soup"),
                    AnswerOption("opt1_v8", "V8"),
                    AnswerOption("opt1_marinara", "Marinara"),
                    AnswerOption("opt1_tomato_sauce", "Tomato Sauce")
                ),
                correctAnswerOptionId = "opt1_marinara",
                points = 15
            ),
            TrueFalseQuestion(
                id = "tf2",
                text = "Water boils at 100 degrees Celsius at sea level.",
                correctAnswer = true,
                points = 5
            ),
            MultipleChoiceQuestion(
                id = "mc2",
                text = "Repository: Which planet is known as the Red Planet?",
                options = listOf(
                    AnswerOption("opt2_earth", "Earth"),
                    AnswerOption("opt2_mars", "Mars"),
                    AnswerOption("opt2_jupiter", "Jupiter"),
                    AnswerOption("opt2_venus", "Venus")
                ),
                correctAnswerOptionId = "opt2_mars",
                points = 12
            )
        )
        return flowOf(sampleQuestions)
    }
}
