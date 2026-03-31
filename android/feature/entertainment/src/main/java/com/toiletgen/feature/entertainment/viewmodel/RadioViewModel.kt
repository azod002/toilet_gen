package com.toiletgen.feature.entertainment.viewmodel

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import com.toiletgen.feature.entertainment.data.RadioStation
import com.toiletgen.feature.entertainment.data.radioStations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RadioUiState(
    val stations: List<RadioStation> = radioStations,
    val currentStation: RadioStation? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class RadioViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RadioUiState())
    val uiState: StateFlow<RadioUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    fun playStation(station: RadioStation) {
        if (_uiState.value.currentStation == station && _uiState.value.isPlaying) {
            stop()
            return
        }
        stop()
        _uiState.value = _uiState.value.copy(
            currentStation = station,
            isLoading = true,
            error = null,
        )
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(station.streamUrl)
                setOnPreparedListener {
                    it.start()
                    _uiState.value = _uiState.value.copy(isPlaying = true, isLoading = false)
                }
                setOnErrorListener { _, _, _ ->
                    _uiState.value = _uiState.value.copy(
                        isPlaying = false,
                        isLoading = false,
                        error = "Не удалось воспроизвести",
                    )
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isPlaying = false,
                isLoading = false,
                error = e.message,
            )
        }
    }

    fun stop() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        _uiState.value = _uiState.value.copy(isPlaying = false, isLoading = false)
    }

    override fun onCleared() {
        stop()
        super.onCleared()
    }
}
