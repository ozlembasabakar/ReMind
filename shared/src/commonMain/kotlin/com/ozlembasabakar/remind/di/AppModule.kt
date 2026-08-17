package com.ozlembasabakar.remind.di

import com.ozlembasabakar.remind.data.audio.createAudioPlayer
import com.ozlembasabakar.remind.data.repository.KtorVocabularyRepository
import com.ozlembasabakar.remind.data.repository.VocabularyRepository
import com.ozlembasabakar.remind.domain.usecase.GetNextFlashcardUseCase
import com.ozlembasabakar.remind.domain.usecase.PlayAudioUseCase
import com.ozlembasabakar.remind.domain.usecase.ProcessSrsReviewUseCase
import com.ozlembasabakar.remind.presentation.FlashcardViewModel

object AppModule {

    private val repository: VocabularyRepository by lazy {
        KtorVocabularyRepository()
    }

    private val getNextFlashcardUseCase by lazy {
        GetNextFlashcardUseCase(repository)
    }

    private val processSrsReviewUseCase by lazy {
        ProcessSrsReviewUseCase(repository)
    }

    private val playAudioUseCase by lazy {
        PlayAudioUseCase(createAudioPlayer())
    }

    fun provideFlashcardViewModel(): FlashcardViewModel {
        return FlashcardViewModel(
            getNextFlashcardUseCase = getNextFlashcardUseCase,
            processSrsReviewUseCase = processSrsReviewUseCase,
            playAudioUseCase = playAudioUseCase
        )
    }
}
