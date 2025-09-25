package com.cricut.androidassessment.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cricut.androidassessment.data.model.AnswerOption
import com.cricut.androidassessment.data.model.MultipleChoiceQuestion
import com.cricut.androidassessment.data.model.QuizUiState
import com.cricut.androidassessment.data.model.TrueFalseQuestion
import com.cricut.androidassessment.ui.AssessmentViewModel
import com.cricut.androidassessment.ui.theme.AndroidAssessmentTheme

@Composable
fun AssessmentScreen(
    modifier: Modifier = Modifier,
    viewModel: AssessmentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> LoadingScreen(modifier)
        uiState.questions.isEmpty() -> EmptyStateScreen(modifier)
        uiState.isQuizComplete -> QuizCompleteScreen(
            modifier = modifier,
            score = uiState.score,
            totalQuestions = uiState.questions.size
        )
        else -> QuizContent(
            modifier = modifier,
            uiState = uiState,
            onAnswerSelected = { questionId, answer ->
                viewModel.selectAnswer(questionId, answer)
            },
            onNextClicked = { viewModel.nextQuestion() }
        )
    }
}

@Composable
fun QuizCompleteScreen(
    modifier: Modifier = Modifier,
    score: Int,
    totalQuestions: Int
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Quiz Complete!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your score: $score / ${totalQuestions * 10}",
            style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(8.dp))
        Text("Loading Questions...")
    }
}

@Composable
fun EmptyStateScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No questions available at the moment.")
    }
}

@Composable
fun QuizContent(
    modifier: Modifier = Modifier,
    uiState: QuizUiState,
    onAnswerSelected: (questionId: String, answer: String) -> Unit,
    onNextClicked: () -> Unit
) {
    if (uiState.currentQuestionIndex >= uiState.questions.size) {
        Text("Error: Question index out of bounds.", modifier = modifier.padding(16.dp))
        return
    }

    val currentQuestion = uiState.questions[uiState.currentQuestionIndex]

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Question ${uiState.currentQuestionIndex + 1} of ${uiState.questions.size}",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = "Score: ${uiState.score}",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (currentQuestion) {
                is TrueFalseQuestion -> {
                    TrueFalseQuestionUI(
                        question = currentQuestion,
                        selectedAnswer = uiState.userAnswers[currentQuestion.id]?.toBooleanStrictOrNull(),
                        onAnswerSelected = { answerBoolean ->
                            onAnswerSelected(currentQuestion.id, answerBoolean.toString())
                        }
                    )
                }
                is MultipleChoiceQuestion -> {
                    MultipleChoiceQuestionUI(
                        question = currentQuestion,
                        selectedOptionId = uiState.userAnswers[currentQuestion.id],
                        onOptionSelected = { optionId ->
                            onAnswerSelected(currentQuestion.id, optionId)
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Spacer(modifier = Modifier.weight(1f)) // Placeholder for Back button
            Button(
                onClick = onNextClicked,
                enabled = uiState.userAnswers.containsKey(currentQuestion.id)
            ) {
                Text(if (uiState.currentQuestionIndex < uiState.questions.size - 1) "Next" else "Finish")
            }
        }
    }
}

@Composable
fun TrueFalseQuestionUI(
    question: TrueFalseQuestion,
    selectedAnswer: Boolean?, // User's previously selected answer, if any
    onAnswerSelected: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = question.text,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(0.8f)
        ) {
            Button(
                onClick = { onAnswerSelected(true) },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
            ) {
                Text("True")
            }
            Button(
                onClick = { onAnswerSelected(false) },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
            ) {
                Text("False")
            }
        }
        selectedAnswer?.let {
            Text(
                "You selected: $it",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun MultipleChoiceQuestionUI(
    question: MultipleChoiceQuestion,
    selectedOptionId: String?,
    onOptionSelected: (optionId: String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = question.text,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        question.options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionSelected(option.id) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option.id == selectedOptionId),
                    onClick = { onOptionSelected(option.id) },
                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = option.text, style = MaterialTheme.typography.bodyLarge)
            }
        }

        selectedOptionId?.let {
            val selectedText = question.options.find { opt -> opt.id == it }?.text ?: "N/A"
            Text(
                "You selected: $selectedText",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}


@Preview(name = "Assessment Screen - Default State", showBackground = true)
@Composable
fun AssessmentScreenDefaultPreview() {
    AndroidAssessmentTheme {
        AssessmentScreen()
    }
}


@Preview(name = "Multiple Choice Question UI", showBackground = true)
@Composable
fun MultipleChoiceQuestionUIPreview() {
    AndroidAssessmentTheme {
        MultipleChoiceQuestionUI(
            question = MultipleChoiceQuestion(
                id = "mc_preview",
                text = "Which of these is a primary color?",
                options = listOf(
                    AnswerOption("opt_red", "Red"),
                    AnswerOption("opt_green", "Green"),
                    AnswerOption("opt_orange", "Orange")
                ),
                correctAnswerOptionId = "opt_red",
                points = 10
            ),
            selectedOptionId = "opt_green",
            onOptionSelected = {}
        )
    }
}

@Preview(name = "Quiz Content - Multiple Choice", showBackground = true)
@Composable
fun QuizContentPreview_MultipleChoice() {
    AndroidAssessmentTheme {
        val sampleMCQ = MultipleChoiceQuestion(
            id = "mc_content_preview",
            text = "Preview: Which planet is known as the Red Planet?",
            options = listOf(
                AnswerOption("p_earth", "Earth"),
                AnswerOption("p_mars", "Mars"),
                AnswerOption("p_jupiter", "Jupiter")
            ),
            correctAnswerOptionId = "p_mars",
            points = 5
        )
        val sampleUiState = QuizUiState(
            currentQuestionIndex = 0,
            questions = listOf(sampleMCQ),
            userAnswers = mapOf(sampleMCQ.id to "p_mars"),
            isLoading = false,
            score = 5
        )
        QuizContent(
            uiState = sampleUiState,
            onAnswerSelected = { _, _ -> },
            onNextClicked = {}
        )
    }
}

@Preview(name = "Quiz Complete Screen", showBackground = true)
@Composable
fun QuizCompleteScreenPreview() {
    AndroidAssessmentTheme {
        QuizCompleteScreen(score = 80, totalQuestions = 10)
    }
}

@Preview(name = "Loading State", showBackground = true)
@Composable
fun LoadingScreenPreview() {
    AndroidAssessmentTheme {
        LoadingScreen()
    }
}

@Preview(name = "Empty State", showBackground = true)
@Composable
fun EmptyStateScreenPreview() {
    AndroidAssessmentTheme {
        EmptyStateScreen()
    }
}

@Preview(name = "True/False Question UI", showBackground = true)
@Composable
fun TrueFalseQuestionUIPreview() {
    AndroidAssessmentTheme {
        TrueFalseQuestionUI(
            question = TrueFalseQuestion(
                id = "tf_preview",
                text = "Is Jetpack Compose awesome for UI development in Android?",
                correctAnswer = true,
                points = 5
            ),
            selectedAnswer = true,
            onAnswerSelected = {}
        )
    }
}

@Preview(name = "True/False Question UI - No Answer Selected", showBackground = true)
@Composable
fun TrueFalseQuestionUINoAnswerPreview() {
    AndroidAssessmentTheme {
        TrueFalseQuestionUI(
            question = TrueFalseQuestion(
                id = "tf_preview_2",
                text = "Is XML still required for all Android layouts?",
                correctAnswer = false,
                points = 5
            ),
            selectedAnswer = null,
            onAnswerSelected = {}
        )
    }
}



@Preview(name = "Quiz Content - True/False", showBackground = true)
@Composable
fun QuizContentPreview_TrueFalse() {
    AndroidAssessmentTheme {
        val sampleTrueFalseQuestion = TrueFalseQuestion(
            id = "tf_content_preview",
            text = "Preview: The sky is typically blue during the day.",
            correctAnswer = true,
            points = 10
        )
        val sampleUiState = QuizUiState(
            currentQuestionIndex = 0,
            questions = listOf(sampleTrueFalseQuestion),
            userAnswers = mapOf(sampleTrueFalseQuestion.id to "true"),
            isLoading = false,
            isQuizComplete = false,
            score = 10
        )
        QuizContent(
            uiState = sampleUiState,
            onAnswerSelected = { _, _ -> },
            onNextClicked = {}
        )
    }
}

@Preview(name = "Quiz Content - No Questions (Edge Case)", showBackground = true)
@Composable
fun QuizContentPreview_NoQuestions() {
    AndroidAssessmentTheme {
        val sampleUiState = QuizUiState(
            currentQuestionIndex = 0,
            questions = emptyList(),
            isLoading = false
        )
        QuizContent(
            uiState = sampleUiState,
            onAnswerSelected = { _, _ -> },
            onNextClicked = {}
        )
    }
}
