package com.example.ytsubtitlespeaker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ytsubtitlespeaker.data.model.CaptionTrack
import com.example.ytsubtitlespeaker.data.model.TargetLanguage
import com.example.ytsubtitlespeaker.ui.AppUiState

@Composable
fun CaptionListScreen(
    uiState: AppUiState,
    onSelectTrack: (CaptionTrack) -> Unit,
    onSelectLanguage: (TargetLanguage) -> Unit,
    onProcess: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Altyazı Track Seçimi")
        LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            items(uiState.captionTracks) { track ->
                Text(
                    text = "${track.language} - ${track.name}",
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .clickable { onSelectTrack(track) }
                        .padding(vertical = 8.dp)
                )
            }
        }

        Text(
            text = "Hedef Dil: ${uiState.selectedLanguage.label}",
            modifier = Modifier
                .clickable { expanded = true }
                .padding(top = 8.dp)
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TargetLanguage.entries.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(lang.label) },
                    onClick = {
                        onSelectLanguage(lang)
                        expanded = false
                    }
                )
            }
        }

        Button(onClick = onProcess, modifier = Modifier.padding(top = 12.dp)) {
            Text("Altyazıyı İndir, Çevir ve Sonuca Git")
        }

        if (uiState.statusMessage.isNotBlank()) {
            Text(text = uiState.statusMessage, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
