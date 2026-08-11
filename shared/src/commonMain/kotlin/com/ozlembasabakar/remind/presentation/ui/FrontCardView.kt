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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
fun FrontCardView(
    vocabulary: Vocabulary,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Image displaying feature commented out for v1 release per requirements:
            /*
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF4F4F3)),
                contentAlignment = Alignment.Center
            ) {
                if (!vocabulary.imageUrl.isNullOrEmpty()) {
                    coil3.compose.SubcomposeAsyncImage(
                        model = vocabulary.imageUrl,
                        contentDescription = vocabulary.germanWord,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color(0xFF9E9E9E),
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        error = { state ->
                            println("Coil Image Load Error for '${vocabulary.germanWord}' (URL: ${vocabulary.imageUrl}): ${state.result.throwable.message}")
                            Text(
                                text = "📷",
                                fontSize = 40.sp
                            )
                        }
                    )
                } else {
                    Text(
                        text = "📷",
                        fontSize = 40.sp
                    )
                }
            }
            */

            Spacer(modifier = Modifier.height(24.dp))

            // Below Image: German Word Title Centered Horizontally
            Text(
                text = vocabulary.germanWord,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1C1B1F))
                    .clickable { onFlip() }
                    .padding(horizontal = 32.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = AppIcons.FlipCard,
                        contentDescription = "Flip Card",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp).align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = "Flip Card",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
