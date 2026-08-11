package com.ozlembasabakar.remind.presentation.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ozlembasabakar.remind.domain.model.SrsStatus
import com.ozlembasabakar.remind.presentation.FlashcardContract
import com.ozlembasabakar.remind.presentation.FlashcardViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FlashcardScreen(
    viewModel: FlashcardViewModel,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is FlashcardContract.UiEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is FlashcardContract.UiEffect.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onIntent(FlashcardContract.UiIntent.UndoLastRating)
                    }
                }
                is FlashcardContract.UiEffect.AudioPlaybackFailed -> {
                    snackbarHostState.showSnackbar("Audio error: ${effect.errorReason}")
                }
                is FlashcardContract.UiEffect.StudySessionCompleted -> {
                    snackbarHostState.showSnackbar("🎉 Session Completed!")
                }
                is FlashcardContract.UiEffect.NavigateBack -> {
                    onNavigateBack?.invoke()
                }
            }
        }
    }

    val screenScrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF9F9F8)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(screenScrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(48.dp)) {
                    /*
                    IconButton(
                        onClick = { viewModel.onIntent(FlashcardContract.UiIntent.NavigateBack) }
                    ) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF616161)
                        )
                    }
                    */
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            viewModel.onIntent(FlashcardContract.UiIntent.PlayAudio)
                        }
                    ) {
                        Icon(
                            imageVector = AppIcons.VolumeUp,
                            contentDescription = "Audio Pronunciation",
                            tint = if (uiState.isAudioPlaying) MaterialTheme.colorScheme.primary else Color(0xFF757575)
                        )
                    }

                    /*
                    IconButton(
                        onClick = { viewModel.onIntent(FlashcardContract.UiIntent.ToggleBookmark) }
                    ) {
                        Icon(
                            imageVector = if (uiState.isBookmarked) AppIcons.Bookmark else AppIcons.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (uiState.isBookmarked) Color(0xFFFFB300) else Color(0xFF757575)
                        )
                    }
                    */
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }

                    uiState.currentCard != null -> {
                        val currentCard = uiState.currentCard!!
                        val isFront = uiState.cardSide is FlashcardContract.CardSide.Front

                        val rotation by animateFloatAsState(
                            targetValue = if (isFront) 0f else 180f,
                            animationSpec = tween(durationMillis = 400)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationY = rotation
                                    cameraDistance = 12f * density
                                }
                        ) {
                            if (rotation <= 90f) {
                                FrontCardView(
                                    vocabulary = currentCard,
                                    onFlip = { viewModel.onIntent(FlashcardContract.UiIntent.FlipCard) }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { rotationY = 180f }
                                ) {
                                    BackCardView(
                                        vocabulary = currentCard,
                                        onSeeFront = { viewModel.onIntent(FlashcardContract.UiIntent.SeeFront) }
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "🎉", fontSize = 56.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "All flashcards completed!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1B1F)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.onIntent(FlashcardContract.UiIntent.Retry) }
                            ) {
                                Text("Restart Session")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Neutralized SRS Rating Buttons Footer (Hard / Good / Easy commented out for now per request), feature commented out for v1 release per requirements
            /*
            if (uiState.currentCard != null && uiState.cardSide is FlashcardContract.CardSide.Back) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SrsFooterButton(
                        label = "Hard",
                        iconText = "◌",
                        isActive = false,
                        onClick = { viewModel.onIntent(FlashcardContract.UiIntent.SubmitSrsRating(SrsStatus.Rating.HARD)) },
                        modifier = Modifier.weight(1f)
                    )

                    SrsFooterButton(
                        label = "Good",
                        iconText = "✓",
                        isActive = false,
                        onClick = { viewModel.onIntent(FlashcardContract.UiIntent.SubmitSrsRating(SrsStatus.Rating.GOOD)) },
                        modifier = Modifier.weight(1f)
                    )

                    SrsFooterButton(
                        label = "Easy",
                        iconText = "✓",
                        isActive = true,
                        onClick = { viewModel.onIntent(FlashcardContract.UiIntent.SubmitSrsRating(SrsStatus.Rating.EASY)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            */

            if (uiState.currentCard != null && uiState.cardSide is FlashcardContract.CardSide.Back) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF1C1B1F))
                        .clickable { viewModel.onIntent(FlashcardContract.UiIntent.SubmitSrsRating(SrsStatus.Rating.GOOD)) }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Next Word",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = AppIcons.ArrowForward,
                            contentDescription = "Next Word",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SrsFooterButton(
    label: String,
    iconText: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isActive) Color(0xFFE8F5E9) else Color.White
    val contentColor = if (isActive) Color(0xFF2E7D32) else Color(0xFF616161)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = iconText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}
