package com.ozlembasabakar.remind.presentation.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozlembasabakar.remind.domain.model.SrsStatus
import com.ozlembasabakar.remind.domain.usecase.GetNextFlashcardUseCase
import com.ozlembasabakar.remind.domain.usecase.PlayAudioUseCase
import com.ozlembasabakar.remind.domain.usecase.ProcessSrsReviewUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FlashcardViewModel(
    private val getNextFlashcardUseCase: GetNextFlashcardUseCase,
    private val processSrsReviewUseCase: ProcessSrsReviewUseCase,
    private val playAudioUseCase: PlayAudioUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlashcardContract.UiState())
    val uiState: StateFlow<FlashcardContract.UiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<FlashcardContract.UiEffect>()
    val uiEffect: SharedFlow<FlashcardContract.UiEffect> = _uiEffect.asSharedFlow()

    init {
        onIntent(FlashcardContract.UiIntent.LoadNextCard)
    }

    fun onIntent(intent: FlashcardContract.UiIntent) {
        when (intent) {
            is FlashcardContract.UiIntent.LoadNextCard -> loadNextCard()
            is FlashcardContract.UiIntent.FlipCard -> showBackCard()
            is FlashcardContract.UiIntent.SeeFront -> showFrontCard()
            is FlashcardContract.UiIntent.PlayAudio -> playAudio(intent.audioUrl)
            is FlashcardContract.UiIntent.SubmitSrsRating -> submitRating(intent.rating)
            is FlashcardContract.UiIntent.UndoLastRating -> undoLastRating()
            is FlashcardContract.UiIntent.ToggleBookmark -> toggleBookmark()
            is FlashcardContract.UiIntent.NavigateBack -> navigateBack()
            is FlashcardContract.UiIntent.Retry -> loadNextCard()
        }
    }

    private fun loadNextCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val card = getNextFlashcardUseCase()
            if (card != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentCard = card,
                        cardSide = FlashcardContract.CardSide.Front,
                        isBookmarked = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, currentCard = null) }
                _uiEffect.emit(FlashcardContract.UiEffect.StudySessionCompleted)
            }
        }
    }

    private fun showBackCard() {
        _uiState.update { it.copy(cardSide = FlashcardContract.CardSide.Back) }
    }

    private fun showFrontCard() {
        _uiState.update { it.copy(cardSide = FlashcardContract.CardSide.Front) }
    }

    private fun toggleBookmark() {
        _uiState.update { it.copy(isBookmarked = !it.isBookmarked) }
        viewModelScope.launch {
            val bookmarkedNow = _uiState.value.isBookmarked
            val msg = if (bookmarkedNow) "Card saved to bookmarks" else "Removed from bookmarks"
            _uiEffect.emit(FlashcardContract.UiEffect.ShowToast(msg))
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _uiEffect.emit(FlashcardContract.UiEffect.NavigateBack)
        }
    }

    private fun playAudio(url: String?) {
        val targetUrl = url ?: uiState.value.currentCard?.audioUrl
        val germanText = uiState.value.currentCard?.germanWord ?: ""

        if (targetUrl.isNullOrEmpty() && germanText.isEmpty()) {
            viewModelScope.launch {
                _uiEffect.emit(FlashcardContract.UiEffect.AudioPlaybackFailed("No audio or word available"))
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isAudioPlaying = true) }
            runCatching {
                playAudioUseCase(targetUrl, germanText)
            }.onFailure { ex ->
                _uiEffect.emit(
                    FlashcardContract.UiEffect.AudioPlaybackFailed(
                        ex.message ?: "Audio playback failed"
                    )
                )
            }
            _uiState.update { it.copy(isAudioPlaying = false) }
        }
    }

    private var isFlipping = false

    private fun submitRating(rating: SrsStatus.Rating) {
        if (isFlipping) return
        val current = uiState.value.currentCard ?: return
        isFlipping = true

        viewModelScope.launch {
            processSrsReviewUseCase(current.id, rating)
            _uiState.update {
                it.copy(
                    cardSide = FlashcardContract.CardSide.Front,
                    previousCard = current,
                    previousRating = rating
                )
            }
            /*
            _uiEffect.emit(
                FlashcardContract.UiEffect.ShowUndoSnackbar("Rated as ${rating.name.lowercase().replaceFirstChar { c -> c.uppercase() }}")
            )
            */

            // Wait 400ms for card flip animation to finish closing before swapping to nextCard
            kotlinx.coroutines.delay(400)

            val nextCard = getNextFlashcardUseCase()
            if (nextCard != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentCard = nextCard,
                        cardSide = FlashcardContract.CardSide.Front,
                        isBookmarked = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, currentCard = null) }
                _uiEffect.emit(FlashcardContract.UiEffect.StudySessionCompleted)
            }
            isFlipping = false
        }
    }

    private fun undoLastRating() {
        val prev = uiState.value.previousCard ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    currentCard = prev,
                    previousCard = null,
                    previousRating = null,
                    cardSide = FlashcardContract.CardSide.Back
                )
            }
            _uiEffect.emit(FlashcardContract.UiEffect.ShowToast("Rating undone"))
        }
    }
}
