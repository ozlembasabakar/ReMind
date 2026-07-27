package com.ozlembasabakar.remind.domain.model

data class GrammarBreakdown(
    val prasens: String? = null,    // e.g. "geht"
    val prateritum: String? = null, // e.g. "ging"
    val perfekt: String? = null,    // e.g. "ist gegangen"
    val pluralForm: String? = null  // e.g. "die Tische"
)
