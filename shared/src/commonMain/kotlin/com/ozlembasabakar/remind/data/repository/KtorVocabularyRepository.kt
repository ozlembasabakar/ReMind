package com.ozlembasabakar.remind.data.repository

import com.ozlembasabakar.remind.data.remote.RemindApiClient
import com.ozlembasabakar.remind.dto.toDomain
import com.ozlembasabakar.remind.domain.model.SrsStatus
import com.ozlembasabakar.remind.domain.model.Vocabulary
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class KtorVocabularyRepository(
    private val apiClient: RemindApiClient = RemindApiClient()
) : VocabularyRepository {

    private val mutex = Mutex()
    private var dueCardsCache: List<Vocabulary> = emptyList()
    private val reviewedIds = mutableSetOf<String>()

    private suspend fun ensureDueCardsLoaded(): List<Vocabulary> = mutex.withLock {
        if (dueCardsCache.isNotEmpty()) return@withLock dueCardsCache

        try {
            val dtoList = apiClient.getDueWords()
            dueCardsCache = dtoList.map { it.toDomain() }.shuffled()
            dueCardsCache
        } catch (e: Exception) {
            println("Error fetching due cards from Ktor backend: ${e.message}")
            emptyList()
        }
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
            println("Failed to sync SRS review with Ktor backend: ${e.message}")
        }
    }

    override suspend fun getAllCards(): List<Vocabulary> {
        return try {
            val dtoList = apiClient.getAllWords()
            dtoList.map { it.toDomain() }
        } catch (e: Exception) {
            println("Error fetching all cards from Ktor backend: ${e.message}")
            emptyList()
        }
    }
}
