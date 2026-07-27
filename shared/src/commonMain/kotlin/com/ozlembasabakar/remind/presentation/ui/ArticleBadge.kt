package com.ozlembasabakar.remind.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ozlembasabakar.remind.domain.model.Article

@Composable
fun ArticleBadge(
    article: Article,
    modifier: Modifier = Modifier
) {
    if (article == Article.NONE) return

    val (bgColor, textColor) = when (article) {
        Article.DER -> Color(0xFFE3F2FD) to Color(0xFF1E88E5)
        Article.DIE -> Color(0xFFFCE4EC) to Color(0xFFE91E63)
        Article.DAS -> Color(0xFFE8F5E9) to Color(0xFF4CAF50)
        Article.PLURAL -> Color(0xFFFFFDE7) to Color(0xFFFBC02D)
        Article.NONE -> Color(0xFFF5F5F5) to Color(0xFF757575)
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = article.germanText.uppercase(),
            color = textColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            letterSpacing = 1.sp
        )
    }
}
