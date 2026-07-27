package com.ozlembasabakar.remind.domain.usecase

import com.ozlembasabakar.remind.domain.model.Vocabulary
import com.ozlembasabakar.remind.domain.repository.VocabularyRepository

class GetNextFlashcardUseCase(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(): Vocabulary? {
        return repository.getNextDueCard()
    }
}
