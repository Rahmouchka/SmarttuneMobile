package com.isetr.smarttune.media

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Gestionnaire audio responsable UNIQUEMENT de la lecture de musique
 * - Gère le cycle de vie de MediaPlayer
 * - Expose l'état de lecture via LiveData
 * - Pas de logique métier, pas d'API
 */
class AudioPlayerManager(context: Context) {

    private val mediaPlayer = MediaPlayer()

    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentSongUrl = MutableLiveData<String?>(null)
    val currentSongUrl: LiveData<String?> = _currentSongUrl

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    init {
        mediaPlayer.setOnCompletionListener {
            _isPlaying.value = false
            Log.d("AudioPlayerManager", "Song finished playing")
        }

        mediaPlayer.setOnErrorListener { mp, what, extra ->
            val errorMsg = "MediaPlayer Error: what=$what, extra=$extra"
            Log.e("AudioPlayerManager", errorMsg)
            _error.value = errorMsg
            _isPlaying.value = false
            true
        }
    }

    /**
     * Jouer une chanson à partir d'une URL
     */
    fun play(url: String) {
        try {
            Log.d("AudioPlayerManager", "=== PLAY START ===")
            Log.d("AudioPlayerManager", "URL: $url")
            Log.d("AudioPlayerManager", "URL vide? ${url.isEmpty()}")

            // Si une chanson est déjà en cours, l'arrêter
            if (mediaPlayer.isPlaying) {
                Log.d("AudioPlayerManager", "Stopping previous song")
                mediaPlayer.stop()
            }

            Log.d("AudioPlayerManager", "Resetting MediaPlayer")
            mediaPlayer.reset()

            Log.d("AudioPlayerManager", "Setting data source")
            mediaPlayer.setDataSource(url)

            Log.d("AudioPlayerManager", "Preparing async")
            mediaPlayer.prepareAsync()

            mediaPlayer.setOnPreparedListener {
                Log.d("AudioPlayerManager", "OnPreparedListener triggered - starting playback")
                try {
                    mediaPlayer.start()
                    _isPlaying.value = true
                    _currentSongUrl.value = url
                    _error.value = null
                    Log.d("AudioPlayerManager", "Song started playing successfully")
                } catch (e: Exception) {
                    Log.e("AudioPlayerManager", "Error in OnPreparedListener: ${e.message}", e)
                    _error.value = "Erreur: ${e.message}"
                    _isPlaying.value = false
                }
            }
        } catch (e: Exception) {
            val errorMsg = "Erreur lors de la lecture: ${e.message}"
            Log.e("AudioPlayerManager", errorMsg, e)
            _error.value = errorMsg
            _isPlaying.value = false
        }
    }

    /**
     * Mettre en pause la musique
     */
    fun pause() {
        try {
            if (mediaPlayer.isPlaying) {
                Log.d("AudioPlayerManager", "Pausing song")
                mediaPlayer.pause()
                _isPlaying.value = false
                Log.d("AudioPlayerManager", "Song paused successfully")
            } else {
                Log.d("AudioPlayerManager", "MediaPlayer not playing, nothing to pause")
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Erreur lors de la mise en pause: ${e.message}", e)
            _error.value = "Erreur pause: ${e.message}"
        }
    }

    /**
     * Reprendre la musique
     */
    fun resume() {
        try {
            if (!mediaPlayer.isPlaying && _currentSongUrl.value != null) {
                Log.d("AudioPlayerManager", "Resuming song from: ${_currentSongUrl.value}")
                mediaPlayer.start()
                _isPlaying.value = true
                Log.d("AudioPlayerManager", "Song resumed successfully")
            } else if (mediaPlayer.isPlaying) {
                Log.d("AudioPlayerManager", "MediaPlayer already playing")
            } else {
                Log.d("AudioPlayerManager", "No song prepared to resume")
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Erreur lors de la reprise: ${e.message}", e)
            _error.value = "Erreur reprise: ${e.message}"
        }
    }

    /**
     * Arrêter la musique
     */
    fun stop() {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            _isPlaying.value = false
            _currentSongUrl.value = null
            Log.d("AudioPlayerManager", "Song stopped")
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Erreur lors de l'arrêt: ${e.message}")
        }
    }

    /**
     * Libérer les ressources
     */
    fun release() {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
            _isPlaying.value = false
            Log.d("AudioPlayerManager", "MediaPlayer released")
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Erreur lors de la libération: ${e.message}")
        }
    }
}

