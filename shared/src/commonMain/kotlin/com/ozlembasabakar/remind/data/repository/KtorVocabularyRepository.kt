package com.ozlembasabakar.remind.data.repository

import co.touchlab.kermit.Logger
import com.ozlembasabakar.remind.data.remote.RemindApiClient
import com.ozlembasabakar.remind.domain.model.SrsStatus
import com.ozlembasabakar.remind.domain.model.Vocabulary
import com.ozlembasabakar.remind.dto.toDomain
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class KtorVocabularyRepository(
    private val apiClient: RemindApiClient = RemindApiClient(),
    private val fallbackRepository: VocabularyRepository = LocalMockVocabularyRepository()
) : VocabularyRepository {

    private val mutex = Mutex()
    private var dueCardsCache: List<Vocabulary> = emptyList()
    private val reviewedIds = mutableSetOf<String>()

    private suspend fun ensureDueCardsLoaded(): List<Vocabulary> = mutex.withLock {
        if (dueCardsCache.isNotEmpty()) return@withLock dueCardsCache

        try {
            val dtoList = apiClient.getDueWords()
            var cards = dtoList.map { it.toDomain() }
            if (cards.isEmpty()) {
                Logger.withTag("KtorVocabularyRepository")
                    .i { "Backend returned empty due words. Fetching all words as fallback..." }
                val allDtoList = apiClient.getAllWords()
                cards = allDtoList.map { it.toDomain() }
            }
            if (cards.isNotEmpty()) {
                dueCardsCache = cards.shuffled()
                return@withLock dueCardsCache
            }
        } catch (e: Exception) {
            Logger.withTag("KtorVocabularyRepository")
                .w { "Error fetching due cards from Ktor backend: ${e.message}" }
            try {
                val allDtoList = apiClient.getAllWords()
                val cards = allDtoList.map { it.toDomain() }
                if (cards.isNotEmpty()) {
                    dueCardsCache = cards.shuffled()
                    return@withLock dueCardsCache
                }
            } catch (inner: Exception) {
                Logger.withTag("KtorVocabularyRepository")
                    .w { "Error fetching all cards from Ktor backend: ${inner.message}" }
            }
        }

        Logger.withTag("KtorVocabularyRepository")
            .w { "Ktor backend returned no cards or failed to connect. Falling back to local mock repository." }
        val fallbackCards = fallbackRepository.getAllCards()
        dueCardsCache = fallbackCards.shuffled()
        dueCardsCache
    }

    override suspend fun getNextDueCard(): Vocabulary? {
        val dueCards = ensureDueCardsLoaded()
        return mutex.withLock {
            dueCards.firstOrNull { it.id !in reviewedIds }
        }
    }

    override suspend fun getRemainingCardsCount(): Int {
        val dueCards = ensureDueCardsLoaded()
        return mutex.withLock {
            maxOf(0, dueCards.size - reviewedIds.size)
        }
    }

    override suspend fun updateSrsStatus(id: String, rating: SrsStatus.Rating) {
        mutex.withLock {
            reviewedIds.add(id)
        }
        try {
            apiClient.postSrsReview(id, rating.name)
        } catch (e: Exception) {
            Logger.withTag("KtorVocabularyRepository")
                .e(e) { "Failed to sync SRS review with Ktor backend" }
        }
    }

    override suspend fun getAllCards(): List<Vocabulary> {
        return try {
            val dtoList = apiClient.getAllWords()
            val domainCards = dtoList.map { it.toDomain() }
            domainCards.ifEmpty { fallbackRepository.getAllCards() }
        } catch (e: Exception) {
            Logger.withTag("KtorVocabularyRepository")
                .e(e) { "Error fetching all cards from Ktor backend" }
            fallbackRepository.getAllCards()
        }
    }
}
