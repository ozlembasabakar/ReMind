package com.ozlembasabakar.remind

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.ozlembasabakar.remind.di.AppModule
import com.ozlembasabakar.remind.presentation.ui.FlashcardScreen

@Composable
@Preview
fun App() {
    val viewModel = remember {
        AppModule.provideFlashcardViewModel()
    }

    MaterialTheme {
        FlashcardScreen(viewModel = viewModel)
    }
}