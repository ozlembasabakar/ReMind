package com.ozlembasabakar.remind

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.ozlembasabakar.remind.data.audio.createAudioPlayer
import com.ozlembasabakar.remind.data.repository.InMemoryVocabularyRepository
import com.ozlembasabakar.remind.domain.usecase.GetNextFlashcardUseCase
import com.ozlembasabakar.remind.domain.usecase.PlayAudioUseCase
import com.ozlembasabakar.remind.domain.usecase.ProcessSrsReviewUseCase
import com.ozlembasabakar.remind.presentation.study.FlashcardViewModel
import com.ozlembasabakar.remind.presentation.ui.FlashcardScreen

@Composable
@Preview
fun App() {
    val viewModel = remember {
        val repository = InMemoryVocabularyRepository()
        val audioPlayer = createAudioPlayer()
        FlashcardViewModel(
            getNextFlashcardUseCase = GetNextFlashcardUseCase(repository),
            processSrsReviewUseCase = ProcessSrsReviewUseCase(repository),
            playAudioUseCase = PlayAudioUseCase(audioPlayer)
        )
    }

    MaterialTheme {
        FlashcardScreen(viewModel = viewModel)
    }
}