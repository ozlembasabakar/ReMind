package com.ozlembasabakar.remind.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ozlembasabakar.remind.domain.model.Vocabulary

@Composable
fun BackCardView(
    vocabulary: Vocabulary,
    onSeeFront: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                ArticleBadge(article = vocabulary.article)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = vocabulary.germanWord,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )

                Text(
                    text = vocabulary.turkishTranslation,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF757575)
                )

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(modifier = Modifier.height(20.dp))

                vocabulary.grammar?.let { grammar ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF9F9F8))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "GRAMMAR & FORMS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8D8D8B),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            grammar.pluralForm?.let { plural ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Plural",
                                        fontSize = 14.sp,
                                        color = Color(0xFF757575)
                                    )
                                    Text(
                                        text = plural,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1C1B1F)
                                    )
                                }
                            }

                            if (grammar.prasens != null || grammar.prateritum != null || grammar.perfekt != null) {
                                if (grammar.pluralForm != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = Color(0xFFEFEFEF))
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                grammar.prasens?.let { prasens ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Präsens", fontSize = 14.sp, color = Color(0xFF757575))
                                        Text(prasens, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                grammar.prateritum?.let { prateritum ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Präteritum", fontSize = 14.sp, color = Color(0xFF757575))
                                        Text(prateritum, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                grammar.perfekt?.let { perfekt ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Perfekt", fontSize = 14.sp, color = Color(0xFF757575))
                                        Text(perfekt, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                if (vocabulary.examples.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "EXAMPLES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8D8D8B),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        vocabulary.examples.forEach { example ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(top = 4.dp, end = 12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFCCCCCC))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(1.5.dp)
                                            .height(36.dp)
                                            .background(Color(0xFFE0E0E0))
                                    )
                                }

                                Column {
                                    HighlightedExampleSentence(
                                        sentence = example.germanSentence,
                                        targetWord = example.targetWord,
                                        targetWordArticle = example.targetWordArticle
                                    )
                                    Text(
                                        text = example.turkishTranslation,
                                        fontSize = 12.sp,
                                        color = Color(0xFF9E9E9E)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSeeFront() }
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⇆",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF616161)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "See Front",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF616161)
                    )
                }
            }
        }
    }
}
