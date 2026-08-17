package com.ozlembasabakar.remind.dto

import com.ozlembasabakar.remind.domain.model.Article
import com.ozlembasabakar.remind.domain.model.GrammarBreakdown
import com.ozlembasabakar.remind.domain.model.SrsStatus
import com.ozlembasabakar.remind.domain.model.TenseExample
import com.ozlembasabakar.remind.domain.model.Vocabulary
import com.ozlembasabakar.remind.domain.model.WordType
import kotlinx.serialization.Serializable

@Serializable
data class WordDto(
    val id: String? = null,
    val germanWord: String = "",
    val article: String = "NONE",
    val wordType: String = "NOUN",
    val turkishTranslation: String = "",
    val imageUrl: String? = null,
    val grammar: GrammarDto? = null,
    val examples: List<TenseExampleDto> = emptyList(),
    val srsStatus: SrsStatusDto? = null
)

@Serializable
data class GrammarDto(
    val pluralForm: String? = null,
    val prasens: String? = null,
    val prateritum: String? = null,
    val perfekt: String? = null
)

@Serializable
data class TenseExampleDto(
    val germanSentence: String = "",
    val turkishTranslation: String = "",
    val targetWord: String = "",
    val targetWordArticle: String = "NONE"
)

@Serializable
data class SrsStatusDto(
    val repetitions: Int = 0,
    val intervalDays: Double = 1.0,
    val easeFactor: Double = 2.5,
    val lastReviewedAtEpochMs: Long = 0L,
    val nextReviewAtEpochMs: Long = 0L
)

fun WordDto.toDomain(docId: String = id ?: ""): Vocabulary = Vocabulary(
    id = docId,
    germanWord = germanWord,
    article = Article.from(article),
    wordType = WordType.from(wordType),
    turkishTranslation = turkishTranslation,
    imageUrl = imageUrl,
    grammar = grammar?.let {
        GrammarBreakdown(
            pluralForm = it.pluralForm,
            prasens = it.prasens,
            prateritum = it.prateritum,
            perfekt = it.perfekt
        )
    },
    examples = examples.map {
        TenseExample(
            germanSentence = it.germanSentence,
            turkishTranslation = it.turkishTranslation,
            targetWord = it.targetWord.ifEmpty { germanWord },
            targetWordArticle = Article.from(it.targetWordArticle)
        )
    },
    srsStatus = srsStatus?.toDomain() ?: SrsStatus()
)

fun SrsStatusDto.toDomain(): SrsStatus = SrsStatus(
    repetitions = repetitions,
    intervalDays = intervalDays,
    easeFactor = easeFactor,
    lastReviewedAtEpochMs = lastReviewedAtEpochMs,
    nextReviewAtEpochMs = nextReviewAtEpochMs
)

fun Vocabulary.toDto(): WordDto = WordDto(
    id = id,
    germanWord = germanWord,
    article = article.name,
    wordType = wordType.name,
    turkishTranslation = turkishTranslation,
    imageUrl = imageUrl,
    grammar = grammar?.let {
        GrammarDto(
            pluralForm = it.pluralForm,
            prasens = it.prasens,
            prateritum = it.prateritum,
            perfekt = it.perfekt
        )
    },
    examples = examples.map {
        TenseExampleDto(
            germanSentence = it.germanSentence,
            turkishTranslation = it.turkishTranslation,
            targetWord = it.targetWord,
            targetWordArticle = it.targetWordArticle.name
        )
    },
    srsStatus = SrsStatusDto(
        repetitions = srsStatus.repetitions,
        intervalDays = srsStatus.intervalDays,
        easeFactor = srsStatus.easeFactor,
        lastReviewedAtEpochMs = srsStatus.lastReviewedAtEpochMs,
        nextReviewAtEpochMs = srsStatus.nextReviewAtEpochMs
    )
)
