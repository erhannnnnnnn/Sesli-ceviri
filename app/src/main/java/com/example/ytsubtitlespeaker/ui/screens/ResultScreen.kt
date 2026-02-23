package com.example.ytsubtitlespeaker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ytsubtitlespeaker.tts.SpeechController
import com.example.ytsubtitlespeaker.ui.AppUiState

@Composable
fun ResultScreen(uiState: AppUiState) {
    val context = LocalContext.current
    val speechController = remember { SpeechController(context) }

    DisposableEffect(Unit) {
        onDispose { speechController.release() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = "Durum: ${uiState.statusMessage}")

        Text(text = "\nOrijinal Altyazı:\n${uiState.originalText}")
        Text(text = "\nÇeviri:\n${uiState.translatedText}")

        Button(
            onClick = { speechController.speak(uiState.translatedText, uiState.selectedLanguage.code) },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Play")
        }
        Button(
            onClick = { speechController.pause() },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Pause")
        }
        Button(
            onClick = { speechController.stop() },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Stop")
        }
        Button(
            onClick = { speechController.resume() },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Resume")
        }
    }
}
