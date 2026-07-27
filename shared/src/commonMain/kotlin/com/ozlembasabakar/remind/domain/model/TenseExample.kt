package com.ozlembasabakar.remind.domain.model

data class TenseExample(
    val tenseName: String, // e.g. "Präsens", "Präteritum", "Perfekt"
    val germanSentence: String, // e.g. "Ich gehe heute nach Hause."
    val turkishTranslation: String, // e.g. "Bugün eve gidiyorum."
    val targetWord: String, // e.g. "gehe"
    val targetWordArticle: Article = Article.NONE // Used for article color-highlighting
)
