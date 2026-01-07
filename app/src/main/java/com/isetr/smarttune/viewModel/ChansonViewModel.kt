package com.isetr.smarttune.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.isetr.smarttune.data.ChansonRepository
import com.isetr.smarttune.data.dto.ChansonResponse
import com.isetr.smarttune.media.AudioPlayerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class ChansonUiState {
    object Idle : ChansonUiState()
    object Loading : ChansonUiState()
    data class Success(val chansons: List<ChansonResponse>) : ChansonUiState()
    data class Error(val message: String) : ChansonUiState()
}

class ChansonViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChansonRepository(application.applicationContext)
    private val audioPlayerManager = AudioPlayerManager(application.applicationContext)

    private val _uiState = MutableLiveData<ChansonUiState>(ChansonUiState.Idle)
    val uiState: LiveData<ChansonUiState> = _uiState

    // LiveData pour tracer le chanson en lecture
    private val _currentPlayingId = MutableLiveData<Long?>(null)
    val currentPlayingId: LiveData<Long?> = _currentPlayingId

    // Expose l'état du lecteur audio
    val isPlaying: LiveData<Boolean> = audioPlayerManager.isPlaying
    val currentSongUrl: LiveData<String?> = audioPlayerManager.currentSongUrl
    val playerError: LiveData<String?> = audioPlayerManager.error

    // Rechercher des chansons via API
    fun searchChansons(query: String) {
        // NE PAS vérifier si la query est vide - on veut récupérer TOUTES les chansons avec ""
        _uiState.value = ChansonUiState.Loading
        viewModelScope.launch {
            val result = repository.searchChansons(query)
            result.fold(
                onSuccess = { chansons ->
                    _uiState.value = ChansonUiState.Success(chansons)
                },
                onFailure = { error ->
                    _uiState.value = ChansonUiState.Error(error.message ?: "Erreur inconnue")
                }
            )
        }
    }

    // Charger les chansons aléatoires au démarrage
    fun loadRandomChansons(humeur: String = "POP") {
        _uiState.value = ChansonUiState.Loading
        viewModelScope.launch {
            val result = repository.getRandomChansons(humeur)
            result.fold(
                onSuccess = { chansons ->
                    _uiState.value = ChansonUiState.Success(chansons)
                },
                onFailure = { error ->
                    _uiState.value = ChansonUiState.Error(error.message ?: "Erreur inconnue")
                }
            )
        }
    }

    // Jouer une chanson
    fun playChanson(chansonId: Long) {
        android.util.Log.d("ChansonViewModel", "playChanson called for ID: $chansonId")
        _currentPlayingId.value = chansonId
        viewModelScope.launch {
            val result = repository.getChansonDetails(chansonId)
            result.fold(
                onSuccess = { chanson ->
                    android.util.Log.d("ChansonViewModel", "Got chanson: $chanson")
                    android.util.Log.d("ChansonViewModel", "URL: ${chanson.url}")
                    try {
                        if (chanson.url != null && chanson.url.isNotEmpty()) {
                            android.util.Log.d("ChansonViewModel", "Playing: ${chanson.url}")
                            audioPlayerManager.play(chanson.url)
                        } else {
                            android.util.Log.d("ChansonViewModel", "URL is null or empty")
                            _uiState.value = ChansonUiState.Error("URL de chanson non disponible")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ChansonViewModel", "Error during playback: ${e.message}", e)
                        _uiState.value = ChansonUiState.Error("Erreur: ${e.message}")
                    }
                },
                onFailure = { error ->
                    android.util.Log.e("ChansonViewModel", "Error fetching chanson: ${error.message}")
                    _uiState.value = ChansonUiState.Error("Erreur: ${error.message}")
                }
            )
        }
    }

    // Mettre en pause
    fun pauseChanson() {
        android.util.Log.d("ChansonViewModel", "pauseChanson called")
        audioPlayerManager.pause()
        viewModelScope.launch {
            // Petit délai pour laisser le MediaPlayer bien se mettre en pause
            delay(100)
            android.util.Log.d("ChansonViewModel", "Pause applied, updating UI")
        }
    }

    // Reprendre
    fun resumeChanson() {
        android.util.Log.d("ChansonViewModel", "resumeChanson called")
        audioPlayerManager.resume()
    }

    // Arrêter
    fun stopChanson() {
        android.util.Log.d("ChansonViewModel", "stopChanson called")
        audioPlayerManager.stop()
        _currentPlayingId.value = null
    }

    // Libérer les ressources au déstruction du ViewModel
    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.release()
    }
}

