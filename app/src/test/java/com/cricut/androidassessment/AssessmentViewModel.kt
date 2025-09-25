package com.cricut.androidassessment

import com.cricut.androidassessment.data.model.TrueFalseQuestion
import com.cricut.androidassessment.ui.AssessmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        assertEquals("Should have 2 questions loaded by default", 2, uiState.questions.size)
    }

    @Test
    fun `selectAnswer for TrueFalseQuestion updates userAnswers correctly`() = runTest(testDispatcher) {
        // Ensure questions are loaded
        val initialUiState = viewModel.uiState.value
        val firstQuestion = initialUiState.questions.firstOrNull { it is TrueFalseQuestion } as? TrueFalseQuestion
        assertNotNull("A TrueFalseQuestion should be available", firstQuestion)

        firstQuestion?.let { question ->
            // Select "true"
            viewModel.selectAnswer(question.id, true)
            var updatedUiState = viewModel.uiState.value
            assertEquals("true", updatedUiState.userAnswers[question.id])

            // Select "false"
            viewModel.selectAnswer(question.id, false)
            updatedUiState = viewModel.uiState.value
            assertEquals("false", updatedUiState.userAnswers[question.id])
        }
    }

    @Test
    fun `selectAnswer for non-existent questionId does not crash and logs error (manual check)`() = runTest(testDispatcher) {
        // Test mostly ensures no crash.
        viewModel.selectAnswer("non_existent_id", true)
        // No assertion needed here other than it didn't crash
        assertTrue(true) // Test passes if it reaches here
    }

    @Test
    fun `selectAnswer for question of wrong type does not crash (manual check)`() = runTest(testDispatcher) {
        //TODO: once other question types are established
        val questionIdForPotentialOtherType = "someOtherId"
        viewModel.selectAnswer(questionIdForPotentialOtherType, true)
        assertTrue(true) // Test passes if it reaches here
    }
}
