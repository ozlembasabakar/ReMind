package com.ozlembasabakar.remind.domain.model

enum class WordType {
    NOUN,
    VERB,
    ADJECTIVE,
    ADVERB,
    PHRASE;

    companion object {
        fun from(value: String?): WordType =
            value?.trim()?.uppercase()?.let { str ->
                runCatching { WordType.valueOf(str) }.getOrNull()
            } ?: NOUN
    }
}
