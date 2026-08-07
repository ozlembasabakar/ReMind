package com.ozlembasabakar.remind.data.remote.dto

import com.ozlembasabakar.remind.domain.model.Article
import com.ozlembasabakar.remind.domain.model.GrammarBreakdown
import com.ozlembasabakar.remind.domain.model.TenseExample
import com.ozlembasabakar.remind.domain.model.Vocabulary
import com.ozlembasabakar.remind.domain.model.WordType
import kotlinx.serialization.Serializable

@Serializable
data class WordDto(
    val germanWord: String = "",
    val article: String = "NONE",
    val wordType: String = "NOUN",
    val turkishTranslation: String = "",
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val grammar: GrammarDto? = null,
    val examples: List<TenseExampleDto> = emptyList()
)

@Serializable
data class GrammarDto(
    val pluralForm: String? = null,
    val prasens: String? = null,
    val prateritum: String? = null,
    val perfekt: String? = null,
    val pastTense: String? = null,
    val perfectTense: String? = null
)

@Serializable
data class TenseExampleDto(
    val germanSentence: String = "",
    val turkishTranslation: String = "",
    val sentenceGerman: String = "",
    val sentenceTurkish: String = "",
    val targetWord: String = "",
    val targetWordArticle: String = "NONE"
)

fun WordDto.toDomain(documentId: String): Vocabulary = Vocabulary(
    id = documentId,
    germanWord = germanWord,
    article = runCatching { Article.valueOf(article.uppercase()) }.getOrDefault(Article.NONE),
    wordType = runCatching { WordType.valueOf(wordType.uppercase()) }.getOrDefault(WordType.NOUN),
    turkishTranslation = turkishTranslation,
    imageUrl = imageUrl,
    audioUrl = audioUrl,
    grammar = grammar?.let {
        GrammarBreakdown(
            pluralForm = it.pluralForm,
            prasens = it.prasens,
            prateritum = it.prateritum ?: it.pastTense,
            perfekt = it.perfekt ?: it.perfectTense
        )
    },
    examples = examples.map {
        val german = it.germanSentence.ifEmpty { it.sentenceGerman }
        val turkish = it.turkishTranslation.ifEmpty { it.sentenceTurkish }
        TenseExample(
            germanSentence = german,
            turkishTranslation = turkish,
            targetWord = it.targetWord.ifEmpty { germanWord },
            targetWordArticle = runCatching { Article.valueOf(it.targetWordArticle.uppercase()) }.getOrDefault(Article.NONE)
        )
    }
)
