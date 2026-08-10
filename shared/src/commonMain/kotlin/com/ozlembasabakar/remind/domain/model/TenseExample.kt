package com.ozlembasabakar.remind.domain.model

data class TenseExample(
    val germanSentence: String,
    val turkishTranslation: String,
    val targetWord: String,
    val targetWordArticle: Article = Article.NONE
)
