package com.ozlembasabakar.remind.domain.model

enum class Article(
    val germanText: String
) {
    DER("der"),
    DIE("die"),
    DAS("das"),
    PLURAL("die (Pl.)"),
    NONE("");

    companion object {
        fun from(value: String?): Article =
            value?.trim()?.uppercase()?.let { str ->
                runCatching { Article.valueOf(str) }.getOrNull()
            } ?: NONE
    }
}
