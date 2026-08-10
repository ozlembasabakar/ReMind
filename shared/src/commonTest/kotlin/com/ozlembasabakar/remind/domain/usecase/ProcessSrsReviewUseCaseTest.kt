package com.ozlembasabakar.remind.domain.usecase

import com.ozlembasabakar.remind.data.repository.LocalMockVocabularyRepository
import com.ozlembasabakar.remind.domain.model.SrsStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ProcessSrsReviewUseCaseTest {

    @Test
    fun testSrsRatingUpdatesIntervalAndRepetitions() = runTest {
        val repository = LocalMockVocabularyRepository()
        val getNextCard = GetNextFlashcardUseCase(repository)
        val processSrs = ProcessSrsReviewUseCase(repository)

        val cardBefore = getNextCard()
        assertNotNull(cardBefore)
        assertEquals("1", cardBefore.id)

        // Submit EASY rating
        processSrs(cardBefore.id, SrsStatus.Rating.EASY)

        val allCards = repository.getAllCards()
        val updatedCard = allCards.first { it.id == cardBefore.id }

        assertEquals(1, updatedCard.srsStatus.repetitions)
        assertEquals(1.5, updatedCard.srsStatus.intervalDays)
    }

    @Test
    fun testHardRatingResetsRepetitions() = runTest {
        val repository = LocalMockVocabularyRepository()
        val processSrs = ProcessSrsReviewUseCase(repository)

        processSrs("1", SrsStatus.Rating.EASY)
        processSrs("1", SrsStatus.Rating.HARD)

        val allCards = repository.getAllCards()
        val updatedCard = allCards.first { it.id == "1" }

        assertEquals(0, updatedCard.srsStatus.repetitions)
    }
}
