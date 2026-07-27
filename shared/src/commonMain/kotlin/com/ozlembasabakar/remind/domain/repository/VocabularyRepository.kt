package com.ozlembasabakar.remind.domain.repository

import com.ozlembasabakar.remind.domain.model.SrsStatus
import com.ozlembasabakar.remind.domain.model.Vocabulary

interface VocabularyRepository {
    suspend fun getNextDueCard(): Vocabulary?
    suspend fun getRemainingCardsCount(): Int
    suspend fun updateSrsStatus(id: String, rating: SrsStatus.Rating)
    suspend fun getAllCards(): List<Vocabulary>
}
