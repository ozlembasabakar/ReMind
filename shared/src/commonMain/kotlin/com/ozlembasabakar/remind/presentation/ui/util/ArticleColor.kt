package com.ozlembasabakar.remind.presentation.ui.util

import androidx.compose.ui.graphics.Color
import com.ozlembasabakar.remind.domain.model.Article

val Article.color: Color
    get() = when (this) {
        Article.DER -> Color(0xFF1E88E5)     // Blue
        Article.DIE -> Color(0xFFE91E63)     // Red / Pink
        Article.DAS -> Color(0xFF4CAF50)     // Green
        Article.PLURAL -> Color(0xFFFBC02D)  // Yellow
        Article.NONE -> Color(0xFF757575)    // Grey
    }
