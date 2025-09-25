package com.cricut.androidassessment

import com.cricut.androidassessment.data.model.MultipleChoiceQuestion
import com.cricut.androidassessment.data.model.TrueFalseQuestion
import com.cricut.androidassessment.ui.AssessmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AssessmentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: AssessmentViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AssessmentViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadQuestions populates questions and sets isLoading to false`() = runTest(testDispatcher) {
        val uiState = viewModel.uiState.value
        assertFalse("isLoading should be false after loadQuestions", uiState.isLoading)
        assertTrue("Questions list should not be empty", uiState.questions.isNotEmpty())
        assertEquals("Should have 2 questions loaded by default", 3, uiState.questions.size)
    }

    @Test
    fun `selectAnswer for TrueFalseQuestion updates userAnswers correctly`() = runTest(testDispatcher) {
        // Ensure questions are loaded
        val initialUiState = viewModel.uiState.value
        val firstQuestion = initialUiState.questions.firstOrNull { it is TrueFalseQuestion } as? TrueFalseQuestion
        assertNotNull("A TrueFalseQuestion should be available", firstQuestion)

        firstQuestion?.let { question ->
            // Select "true"
            viewModel.selectAnswer(question.id, "true")
            var updatedUiState = viewModel.uiState.value
            assertEquals("true", updatedUiState.userAnswers[question.id])

            // Select "false"
            viewModel.selectAnswer(question.id, "false")
            updatedUiState = viewModel.uiState.value
            assertEquals("false", updatedUiState.userAnswers[question.id])
        }
    }

    @Test
    fun `selectAnswer for MultipleChoiceQuestion updates userAnswers correctly`() = runTest {
        val initialUiState = viewModel.uiState.first()
        val mcq = initialUiState.questions.firstOrNull { it is MultipleChoiceQuestion } as? MultipleChoiceQuestion
        assertNotNull("A MultipleChoiceQuestion should be available", mcq)

        mcq?.let { question ->
            val firstOptionId = question.options.first().id
            viewModel.selectAnswer(question.id, firstOptionId)
            val updatedUiState = viewModel.uiState.value
            assertEquals(firstOptionId, updatedUiState.userAnswers[question.id])

            val secondOptionId = question.options[1].id
            viewModel.selectAnswer(question.id, secondOptionId)
            val finalUiState = viewModel.uiState.value
            assertEquals(secondOptionId, finalUiState.userAnswers[question.id])
        }
    }

    @Test
    fun `selectAnswer for non-existent questionId does not crash and logs error (manual check)`() = runTest(testDispatcher) {
        viewModel.selectAnswer("non_existent_id", "true")
        assertTrue(true) // Test passes if it reaches here
    }

    @Test
    fun `nextQuestion advances currentQuestionIndex`() = runTest {
        val initialIndex = viewModel.uiState.value.currentQuestionIndex
        val totalQuestions = viewModel.uiState.value.questions.size
        assertTrue("Should have at least 2 questions for this test", totalQuestions >= 2)


        viewModel.nextQuestion()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(initialIndex + 1, viewModel.uiState.value.currentQuestionIndex)
    }

    @Test
    fun `nextQuestion on last question marks quiz complete and calculates score`() = runTest {
        val totalQuestions = viewModel.uiState.value.questions.size
        assertTrue("Should have questions loaded", totalQuestions > 0)

        // Navigate to the last question
        repeat(totalQuestions - 1) {
            viewModel.nextQuestion()
            testDispatcher.scheduler.advanceUntilIdle()
        }
        assertEquals(totalQuestions - 1, viewModel.uiState.value.currentQuestionIndex)
        assertFalse("Quiz should not be complete yet", viewModel.uiState.value.isQuizComplete)


        // Answer the last question (assuming it's TrueFalse for simplicity in test)
        val lastQuestion = viewModel.uiState.value.questions.last()
        viewModel.selectAnswer(lastQuestion.id, (lastQuestion as TrueFalseQuestion).correctAnswer.toString())
        testDispatcher.scheduler.advanceUntilIdle()

        // Click Next on the last question
        viewModel.nextQuestion()
        testDispatcher.scheduler.advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertTrue("Quiz should be complete", finalState.isQuizComplete)
        assertTrue("Score should be greater than 0 if answered correctly", finalState.score > 0)
    }

    @Test
    fun `calculateScore correctly sums points for mixed question types`() = runTest {
        // Manually answer all questions correctly
        val questions = viewModel.uiState.value.questions
        var expectedScore = 0

        questions.forEach { q ->
            when (q) {
                is TrueFalseQuestion -> {
                    viewModel.selectAnswer(q.id, q.correctAnswer.toString())
                    expectedScore += q.points
                }
                is MultipleChoiceQuestion -> {
                    viewModel.selectAnswer(q.id, q.correctAnswerOptionId)
                    expectedScore += q.points
                }
            }
            testDispatcher.scheduler.advanceUntilIdle()
        }

        // Trigger score calculation
        repeat(questions.size) {
            viewModel.nextQuestion()
            testDispatcher.scheduler.advanceUntilIdle()
        }

        val finalState = viewModel.uiState.value
        assertTrue("Quiz should be complete after answering all and clicking next", finalState.isQuizComplete)
        assertEquals("Calculated score should match expected score", expectedScore, finalState.score)
    }
}
