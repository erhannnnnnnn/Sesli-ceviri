package com.example.ytsubtitlespeaker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ytsubtitlespeaker.ui.AppUiState

@Composable
fun LinkInputScreen(
    uiState: AppUiState,
    onUrlChange: (String) -> Unit,
    onValidateAndNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        OutlinedTextField(
            value = uiState.youtubeUrl,
            onValueChange = onUrlChange,
            label = { Text("YouTube Link") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = onValidateAndNext, modifier = Modifier.padding(top = 12.dp)) {
            Text("Video ID Doğrula ve Devam Et")
        }
        Text(text = "Video ID: ${uiState.videoId}", modifier = Modifier.padding(top = 12.dp))
        if (uiState.statusMessage.isNotBlank()) {
            Text(text = uiState.statusMessage, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
