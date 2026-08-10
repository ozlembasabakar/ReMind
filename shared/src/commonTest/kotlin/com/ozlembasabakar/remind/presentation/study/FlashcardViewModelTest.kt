package com.ozlembasabakar.remind.presentation.study

import com.ozlembasabakar.remind.data.audio.AudioPlayer
import com.ozlembasabakar.remind.data.repository.LocalMockVocabularyRepository
import com.ozlembasabakar.remind.domain.model.SrsStatus
import com.ozlembasabakar.remind.domain.usecase.GetNextFlashcardUseCase
import com.ozlembasabakar.remind.domain.usecase.PlayAudioUseCase
import com.ozlembasabakar.remind.domain.usecase.ProcessSrsReviewUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class FlashcardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    class FakeAudioPlayer : AudioPlayer {
        var spokenText: String? = null
        override suspend fun speakText(text: String) {
            spokenText = text
        }
        override fun stop() {}
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialLoadingAndCardState() = runTest {
        val repository = LocalMockVocabularyRepository()
        val viewModel = FlashcardViewModel(
            GetNextFlashcardUseCase(repository),
            ProcessSrsReviewUseCase(repository),
            PlayAudioUseCase(FakeAudioPlayer())
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        val card = state.currentCard
        assertNotNull(card)
        assertEquals("Tisch", card.germanWord)
        assertEquals(FlashcardContract.CardSide.Front, state.cardSide)
        assertFalse(state.isBookmarked)
    }

    @Test
    fun testFlipCardAndSeeFrontIntents() = runTest {
        val repository = LocalMockVocabularyRepository()
        val viewModel = FlashcardViewModel(
            GetNextFlashcardUseCase(repository),
            ProcessSrsReviewUseCase(repository),
            PlayAudioUseCase(FakeAudioPlayer())
        )

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(FlashcardContract.UiIntent.FlipCard)
        assertEquals(FlashcardContract.CardSide.Back, viewModel.uiState.value.cardSide)

        viewModel.onIntent(FlashcardContract.UiIntent.SeeFront)
        assertEquals(FlashcardContract.CardSide.Front, viewModel.uiState.value.cardSide)
    }

    @Test
    fun testSubmitSrsRatingResetsToFrontAndStoresPreviousCard() = runTest {
        val repository = LocalMockVocabularyRepository()
        val viewModel = FlashcardViewModel(
            GetNextFlashcardUseCase(repository),
            ProcessSrsReviewUseCase(repository),
            PlayAudioUseCase(FakeAudioPlayer())
        )

        testDispatcher.scheduler.advanceUntilIdle()

        // Flip to Back side
        viewModel.onIntent(FlashcardContract.UiIntent.FlipCard)
        assertEquals(FlashcardContract.CardSide.Back, viewModel.uiState.value.cardSide)

        // Submit rating for first card ("Tisch")
        viewModel.onIntent(FlashcardContract.UiIntent.SubmitSrsRating(SrsStatus.Rating.GOOD))
        testDispatcher.scheduler.advanceUntilIdle()

        // State reset: cardSide is reset to Front for the new card ("Katze")
        val state = viewModel.uiState.value
        assertEquals("2", state.currentCard?.id)
        assertEquals(FlashcardContract.CardSide.Front, state.cardSide)
        assertEquals("1", state.previousCard?.id)
    }

    @Test
    fun testUndoLastRatingRestoresPreviousCardOnBackSide() = runTest {
        val repository = LocalMockVocabularyRepository()
        val viewModel = FlashcardViewModel(
            GetNextFlashcardUseCase(repository),
            ProcessSrsReviewUseCase(repository),
            PlayAudioUseCase(FakeAudioPlayer())
        )

        testDispatcher.scheduler.advanceUntilIdle()

        // Rate card 1
        viewModel.onIntent(FlashcardContract.UiIntent.SubmitSrsRating(SrsStatus.Rating.EASY))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("2", viewModel.uiState.value.currentCard?.id)

        // Perform Undo
        viewModel.onIntent(FlashcardContract.UiIntent.UndoLastRating)
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfterUndo = viewModel.uiState.value
        assertEquals("1", stateAfterUndo.currentCard?.id)
        assertEquals(FlashcardContract.CardSide.Back, stateAfterUndo.cardSide)
    }
}
