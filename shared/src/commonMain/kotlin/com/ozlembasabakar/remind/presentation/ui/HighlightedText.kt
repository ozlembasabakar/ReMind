package com.ozlembasabakar.remind.presentation.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.ozlembasabakar.remind.domain.model.Article

@Composable
fun HighlightedExampleSentence(
    sentence: String,
    targetWord: String,
    targetWordArticle: Article = Article.NONE,
    defaultHighlightColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val highlightColor = if (targetWordArticle != Article.NONE) {
        Color(targetWordArticle.hexColor)
    } else {
        defaultHighlightColor
    }

    val annotatedString = buildAnnotatedString {
        val startIndex = sentence.indexOf(targetWord, ignoreCase = true)
        if (startIndex >= 0) {
            append(sentence.substring(0, startIndex))
            withStyle(
                style = SpanStyle(
                    color = highlightColor,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(sentence.substring(startIndex, startIndex + targetWord.length))
            }
            append(sentence.substring(startIndex + targetWord.length))
        } else {
            append(sentence)
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}
