package com.ozlembasabakar.remind.domain.model

data class Vocabulary(
    val id: String,
    val germanWord: String,
    val article: Article = Article.NONE,
    val wordType: WordType,
    val turkishTranslation: String,
    val imageUrl: String? = null,
    val grammar: GrammarBreakdown? = null,
    val examples: List<TenseExample> = emptyList(),
    val srsStatus: SrsStatus = SrsStatus()
)
