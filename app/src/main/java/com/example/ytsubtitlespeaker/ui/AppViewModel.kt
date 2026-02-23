package com.example.ytsubtitlespeaker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ytsubtitlespeaker.BuildConfig
import com.example.ytsubtitlespeaker.data.model.CaptionTrack
import com.example.ytsubtitlespeaker.data.model.TargetLanguage
import com.example.ytsubtitlespeaker.data.network.ApiClient
import com.example.ytsubtitlespeaker.data.repository.SubtitleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(
    val isLoggedIn: Boolean = false,
    val selectedAccount: String = "",
    val youtubeUrl: String = "",
    val videoId: String = "",
    val captionTracks: List<CaptionTrack> = emptyList(),
    val selectedTrack: CaptionTrack? = null,
    val selectedLanguage: TargetLanguage = TargetLanguage.TURKISH,
    val originalText: String = "",
    val translatedText: String = "",
    val isLoading: Boolean = false,
    val statusMessage: String = ""
)

class AppViewModel : ViewModel() {
    private val repository = SubtitleRepository(
        youTubeApi = ApiClient.youtubeApi,
        translationApi = ApiClient.translationApi,
        youtubeApiKey = BuildConfig.YOUTUBE_API_KEY,
        translateApiKey = BuildConfig.TRANSLATE_API_KEY
    )

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    fun onLoginSuccess(email: String) {
        _uiState.update { it.copy(isLoggedIn = true, selectedAccount = email, statusMessage = "Google ile giriş başarılı.") }
    }

    fun onYoutubeUrlChanged(url: String) {
        _uiState.update { it.copy(youtubeUrl = url) }
    }

    fun extractVideoIdAndValidate(): Boolean {
        val id = extractVideoId(_uiState.value.youtubeUrl)
        return if (id != null) {
            _uiState.update { it.copy(videoId = id, statusMessage = "Video ID doğrulandı: $id") }
            true
        } else {
            _uiState.update { it.copy(statusMessage = "Geçerli bir YouTube linki girin.") }
            false
        }
    }

    fun fetchCaptionTracks() {
        val videoId = _uiState.value.videoId
        if (videoId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Altyazılar yükleniyor...") }
            repository.listCaptionTracks(videoId)
                .onSuccess { tracks ->
                    _uiState.update {
                        it.copy(
                            captionTracks = tracks,
                            isLoading = false,
                            statusMessage = if (tracks.isEmpty()) "Bu videoda altyazı track bulunamadı." else "${tracks.size} track bulundu."
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isLoading = false, statusMessage = err.message ?: "Bilinmeyen hata") }
                }
        }
    }

    fun selectTrack(track: CaptionTrack) {
        _uiState.update { it.copy(selectedTrack = track, statusMessage = "Track seçildi: ${track.language} - ${track.name}") }
    }

    fun selectLanguage(language: TargetLanguage) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    fun processTrack() {
        val track = _uiState.value.selectedTrack ?: run {
            _uiState.update { it.copy(statusMessage = "Lütfen bir track seçin.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Altyazı indiriliyor ve çevriliyor...") }
            repository.fetchAndTranslateCaption(track.id, _uiState.value.selectedLanguage.code)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            originalText = result.originalText,
                            translatedText = result.translatedText,
                            isLoading = false,
                            statusMessage = "İşlem tamamlandı."
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusMessage = err.message ?: "İndirme/çeviri sırasında hata oluştu."
                        )
                    }
                }
        }
    }

    private fun extractVideoId(url: String): String? {
        val patterns = listOf(
            Regex("(?:v=|/)([0-9A-Za-z_-]{11}).*"),
            Regex("youtu\\.be/([0-9A-Za-z_-]{11})")
        )

        for (pattern in patterns) {
            val match = pattern.find(url)
            if (match != null) return match.groupValues[1]
        }
        return null
    }
}
