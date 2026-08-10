package com.ozlembasabakar.remind.presentation.study

import com.ozlembasabakar.remind.domain.model.SrsStatus
import com.ozlembasabakar.remind.domain.model.Vocabulary

object FlashcardContract {

    sealed interface CardSide {
        data object Front : CardSide
        data object Back : CardSide
    }

    data class UiState(
        val isLoading: Boolean = true,
        val currentCard: Vocabulary? = null,
        val previousCard: Vocabulary? = null,
        val previousRating: SrsStatus.Rating? = null,
        val cardSide: CardSide = CardSide.Front,
        val remainingCardsCount: Int = 0,
        val isAudioPlaying: Boolean = false,
        val isBookmarked: Boolean = false,
        val isSessionCompleted: Boolean = false,
        val error: String? = null
    )

    sealed interface UiIntent {
        data object LoadNextCard : UiIntent
        data object FlipCard : UiIntent
        data object SeeFront : UiIntent
        data object PlayAudio : UiIntent
        data class SubmitSrsRating(val rating: SrsStatus.Rating) : UiIntent
        data object UndoLastRating : UiIntent
        data object ToggleBookmark : UiIntent
        data object NavigateBack : UiIntent
        data object Retry : UiIntent
    }

    sealed interface UiEffect {
        data class ShowToast(val message: String) : UiEffect
        data class ShowUndoSnackbar(val message: String) : UiEffect
        data object StudySessionCompleted : UiEffect
        data class AudioPlaybackFailed(val errorReason: String) : UiEffect
        data object NavigateBack : UiEffect
    }
}
