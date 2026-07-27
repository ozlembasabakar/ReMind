package com.ozlembasabakar.remind.domain.model

enum class Article(
    val germanText: String,
    val hexColor: Long
) {
    DER("der", 0xFF1E88E5),          // Blue
    DIE("die", 0xFFE91E63),          // Red / Pink
    DAS("das", 0xFF4CAF50),          // Green
    PLURAL("die (Pl.)", 0xFFFBC02D), // Yellow
    NONE("", 0xFF757575)             // Grey
}
