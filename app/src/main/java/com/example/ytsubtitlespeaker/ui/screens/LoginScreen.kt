package com.example.ytsubtitlespeaker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    statusMessage: String,
    onGoogleSignIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "YTSubtitleSpeaker")
        Button(onClick = onGoogleSignIn, modifier = Modifier.padding(top = 16.dp)) {
            Text("Google ile Giriş Yap")
        }
        if (statusMessage.isNotBlank()) {
            Text(text = statusMessage, modifier = Modifier.padding(top = 12.dp))
        }
    }
}
