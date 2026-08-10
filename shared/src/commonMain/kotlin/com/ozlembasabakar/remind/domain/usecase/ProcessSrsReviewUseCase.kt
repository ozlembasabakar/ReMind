package com.ozlembasabakar.remind.domain.usecase

import com.ozlembasabakar.remind.domain.model.SrsStatus
import com.ozlembasabakar.remind.data.repository.VocabularyRepository

class ProcessSrsReviewUseCase(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(id: String, rating: SrsStatus.Rating) {
        repository.updateSrsStatus(id, rating)
    }
}
