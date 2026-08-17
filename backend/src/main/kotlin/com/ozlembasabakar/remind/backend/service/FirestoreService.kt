package com.ozlembasabakar.remind.backend.service

import com.google.cloud.firestore.Firestore
import com.ozlembasabakar.remind.dto.GrammarDto
import com.ozlembasabakar.remind.dto.SrsStatusDto
import com.ozlembasabakar.remind.dto.TenseExampleDto
import com.ozlembasabakar.remind.dto.WordDto
import kotlinx.coroutines.Dispatchers
import com.ozlembasabakar.remind.backend.util.await
import kotlinx.coroutines.withContext

class FirestoreService(private val db: Firestore) {

    private val wordsCollection = db.collection("words")

    suspend fun getAllWords(): List<WordDto> = withContext(Dispatchers.IO) {
        val querySnapshot = wordsCollection.get().await()
        querySnapshot.documents.mapNotNull { doc ->
            try {
                val data = doc.data
                mapDocToWordDto(doc.id, data)
            } catch (e: Exception) {
                println("Failed to parse document ${doc.id}: ${e.message}")
                null
            }
        }
    }

    suspend fun getDueWords(): List<WordDto> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val querySnapshot = wordsCollection
            .whereLessThanOrEqualTo("srsStatus.nextReviewAtEpochMs", now)
            .get()
            .await()

        querySnapshot.documents.mapNotNull { doc ->
            try {
                val data = doc.data
                mapDocToWordDto(doc.id, data)
            } catch (e: Exception) {
                println("Failed to parse due document ${doc.id}: ${e.message}")
                null
            }
        }
    }

    suspend fun updateSrsStatus(wordId: String, rating: String): WordDto? = withContext(Dispatchers.IO) {
        val docRef = wordsCollection.document(wordId)

        val apiFuture = db.runTransaction { transaction ->
            val doc = transaction.get(docRef).get()
            if (!doc.exists()) return@runTransaction null

            val data = doc.data
            val currentDto = mapDocToWordDto(wordId, data)
            val currentSrs = currentDto.srsStatus ?: SrsStatusDto()

            val repetitions = currentSrs.repetitions + 1
            val intervalDays = when (rating.uppercase()) {
                "EASY" -> currentSrs.intervalDays * 2.5
                "GOOD" -> currentSrs.intervalDays * 1.5
                else -> 1.0 // HARD
            }
            val now = System.currentTimeMillis()
            val nextReviewAt = now + (intervalDays * 24 * 60 * 60 * 1000).toLong()

            val updatedSrs = SrsStatusDto(
                repetitions = repetitions,
                intervalDays = intervalDays,
                easeFactor = currentSrs.easeFactor,
                lastReviewedAtEpochMs = now,
                nextReviewAtEpochMs = nextReviewAt
            )

            transaction.update(
                docRef,
                mapOf(
                    "srsStatus.repetitions" to updatedSrs.repetitions,
                    "srsStatus.intervalDays" to updatedSrs.intervalDays,
                    "srsStatus.lastReviewedAtEpochMs" to updatedSrs.lastReviewedAtEpochMs,
                    "srsStatus.nextReviewAtEpochMs" to updatedSrs.nextReviewAtEpochMs
                )
            )

            currentDto.copy(srsStatus = updatedSrs)
        }

        apiFuture.await()
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapDocToWordDto(id: String, map: Map<String, Any>): WordDto {
        val germanWord = map["germanWord"] as? String ?: ""
        val article = map["article"] as? String ?: "NONE"
        val wordType = map["wordType"] as? String ?: "NOUN"
        val turkishTranslation = map["turkishTranslation"] as? String ?: ""
        val imageUrl = map["imageUrl"] as? String

        val grammarMap = map["grammar"] as? Map<String, Any?>
        val grammar = grammarMap?.let { g ->
            GrammarDto(
                pluralForm = g["pluralForm"] as? String,
                prasens = g["prasens"] as? String,
                prateritum = (g["prateritum"] ?: g["pastTense"]) as? String,
                perfekt = (g["perfekt"] ?: g["perfectTense"]) as? String
            )
        }

        val examplesList = map["examples"] as? List<Map<String, Any?>>
        val examples = examplesList?.map { e ->
            TenseExampleDto(
                germanSentence = (e["germanSentence"] ?: e["sentenceGerman"]) as? String ?: "",
                turkishTranslation = (e["turkishTranslation"] ?: e["sentenceTurkish"]) as? String ?: "",
                targetWord = e["targetWord"] as? String ?: germanWord,
                targetWordArticle = e["targetWordArticle"] as? String ?: "NONE"
            )
        } ?: emptyList()

        val srsMap = map["srsStatus"] as? Map<String, Any?>
        val srsStatus = srsMap?.let { s ->
            SrsStatusDto(
                repetitions = (s["repetitions"] as? Long)?.toInt() ?: 0,
                intervalDays = (s["intervalDays"] as? Double) ?: 1.0,
                easeFactor = (s["easeFactor"] as? Double) ?: 2.5,
                lastReviewedAtEpochMs = (s["lastReviewedAtEpochMs"] as? Long) ?: 0L,
                nextReviewAtEpochMs = (s["nextReviewAtEpochMs"] as? Long) ?: 0L
            )
        }

        return WordDto(
            id = id,
            germanWord = germanWord,
            article = article,
            wordType = wordType,
            turkishTranslation = turkishTranslation,
            imageUrl = imageUrl,
            grammar = grammar,
            examples = examples,
            srsStatus = srsStatus
        )
    }
}
