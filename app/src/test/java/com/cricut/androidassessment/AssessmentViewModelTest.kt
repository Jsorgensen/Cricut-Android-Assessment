package com.cricut.androidassessment

import com.cricut.androidassessment.data.model.AnswerOption
import com.cricut.androidassessment.data.model.MultipleChoiceQuestion
import com.cricut.androidassessment.data.model.Question
import com.cricut.androidassessment.data.model.TrueFalseQuestion
import com.cricut.androidassessment.data.repository.QuizRepository
import com.cricut.androidassessment.ui.AssessmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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

    private lateinit var mockQuizRepository: QuizRepository
    private lateinit var viewModel: AssessmentViewModel

    private val testQuestions: List<Question> = listOf(
        TrueFalseQuestion("tf_test1", "Test TF 1: Is this a test?", 5, true),
        MultipleChoiceQuestion(
            "mc_test1", "Test MCQ 1: Choose A",
            10,
            listOf(AnswerOption("a", "A"), AnswerOption("b", "B")),
            "a"
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockQuizRepository = object : QuizRepository {
            override fun getQuizQuestions(): Flow<List<Question>> = flowOf(testQuestions)
        }

        viewModel = AssessmentViewModel(mockQuizRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadQuestionsFromRepository populates questions and sets isLoading to false`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse("isLoading should be false after loadQuestions", uiState.isLoading)
        assertEquals("Questions list should match testQuestions", testQuestions.size, uiState.questions.size)
        assertEquals(testQuestions[0].id, uiState.questions[0].id)
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
        testDispatcher.scheduler.advanceUntilIdle() // Load questions
        val loadedQuestions = viewModel.uiState.value.questions
        assertTrue("Test questions should be loaded", loadedQuestions.isNotEmpty())

        // Navigate to the last question
        repeat(loadedQuestions.size - 1) {
            viewModel.nextQuestion()
            testDispatcher.scheduler.advanceUntilIdle()
        }

        // Answer the last question correctly
        val lastQuestion = loadedQuestions.last()
        var expectedScore = 0
        when (lastQuestion) {
            is TrueFalseQuestion -> {
                viewModel.selectAnswer(lastQuestion.id, lastQuestion.correctAnswer.toString())
                expectedScore = lastQuestion.points
            }
            is MultipleChoiceQuestion -> {
                viewModel.selectAnswer(lastQuestion.id, lastQuestion.correctAnswerOptionId)
                expectedScore = lastQuestion.points
            }
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Click Next on the last question
        viewModel.nextQuestion()
        testDispatcher.scheduler.advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertTrue("Quiz should be complete", finalState.isQuizComplete)
        if(expectedScore > 0) {
            assertEquals("Score should include points from correctly answered last question", expectedScore, finalState.score)
        }
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

    @Test
    fun `previousQuestion decrements currentQuestionIndex and clears quizComplete`() = runTest {
        // Navigate forward first
        viewModel.nextQuestion()
        testDispatcher.scheduler.advanceUntilIdle()
        val currentIndexAfterNext = viewModel.uiState.value.currentQuestionIndex
        assertTrue("currentQuestionIndex should be > 0 after next", currentIndexAfterNext > 0)

        viewModel.previousQuestion()
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfterPrevious = viewModel.uiState.value
        assertEquals(currentIndexAfterNext - 1, stateAfterPrevious.currentQuestionIndex)
        assertFalse("isQuizComplete should be false after previousQuestion", stateAfterPrevious.isQuizComplete)
    }

    @Test
    fun `previousQuestion at first question does not change index`() = runTest {
        val initialIndex = viewModel.uiState.value.currentQuestionIndex
        assertEquals("Should start at index 0", 0, initialIndex)

        viewModel.previousQuestion()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Index should remain 0", 0, viewModel.uiState.value.currentQuestionIndex)
    }
}
