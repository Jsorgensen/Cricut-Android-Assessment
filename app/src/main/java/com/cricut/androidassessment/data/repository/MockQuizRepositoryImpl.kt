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
                id = "android_mc1",
                text = "Which of these is NOT a standard layout Composable in Jetpack Compose?",
                options = listOf(
                    AnswerOption("opt_column", "Column"),
                    AnswerOption("opt_row", "Row"),
                    AnswerOption("opt_box", "Box"),
                    AnswerOption("opt_gridlayout", "GridLayout")
                ),
                correctAnswerOptionId = "opt_gridlayout",
                points = 15
            ),
            TrueFalseQuestion(
                id = "android_tf2",
                text = "True or False: `rememberSaveable` helps preserve state across process death.",
                correctAnswer = true,
                points = 10
            ),
            MultipleChoiceQuestion(
                id = "android_mc2",
                text = "What annotation is used to make an Android Application class usable by Hilt for dependency injection?",
                options = listOf(
                    AnswerOption("opt_injectapp", "@InjectApplication"),
                    AnswerOption("opt_hiltapp", "@HiltAndroidApp"),
                    AnswerOption("opt_androidapp", "@AndroidApplication"),
                    AnswerOption("opt_componentapp", "@ComponentApplication")
                ),
                correctAnswerOptionId = "opt_hiltapp",
                points = 15
            ),
            TrueFalseQuestion(
                id = "android_tf3",
                text = "True or False: `StateFlow` is a hot flow.",
                correctAnswer = true,
                points = 10
            ),
            MultipleChoiceQuestion(
                id = "android_mc3",
                text = "Which component is primarily responsible for observing LiveData or Flow updates in an MVVM architecture?",
                options = listOf(
                    AnswerOption("opt_activity", "Activity/Fragment (UI Controller)"),
                    AnswerOption("opt_viewmodel", "ViewModel"),
                    AnswerOption("opt_repository", "Repository"),
                    AnswerOption("opt_model", "Model")
                ),
                correctAnswerOptionId = "opt_activity",
                points = 10
            )
        )
        return flowOf(sampleQuestions)
    }
}
