# 🎵 SYSTÈME DE LECTURE AUDIO - EXPLICATION COMPLÈTE

## 📌 Vue d'ensemble

Le système de lecture audio respecte MVVM strictement:
- **Fragment** : Observer uniquement (UI)
- **ViewModel** : Orchestre la lecture + gère l'état
- **AudioPlayerManager** : Exécute la lecture (MediaPlayer)
- **Repository** : Fetch l'URL via API

---

## 🎯 Architecture

```
User clique Play
        ↓
ChansonListFragment
        ↓
ChansonAdapter.onClickListener
        ↓
viewModel.playChanson(chansonId)
        ↓
Repository.getChansonDetails(chansonId) → API
        ↓
audioPlayerManager.play(url)
        ↓
MediaPlayer.start()
        ↓
Observer isPlaying → Fragment met à jour UI
```

---

## 🔄 Flux complet

### **1. User clique Play**
```kotlin
// ChansonAdapter.kt
binding.btnPlay.setOnClickListener {
    if (isPlaying && !isPaused) {
        onPauseClick(chanson)  // En lecture → Pause
    } else {
        onPlayClick(chanson)   // Pas en lecture → Play
    }
}
```

### **2. Fragment reçoit le callback**
```kotlin
// ChansonListFragment.kt
adapter = ChansonAdapter(
    onPlayClick = { chanson ->
        viewModel.playChanson(chanson.id)
    },
    onPauseClick = { chanson ->
        viewModel.pauseChanson()
    }
)
```

### **3. ViewModel appelle l'API**
```kotlin
// ChansonViewModel.kt
fun playChanson(chansonId: Long) {
    _currentPlayingId.value = chansonId  // Notifie UI
    viewModelScope.launch {
        val result = repository.getChansonDetails(chansonId)
        result.fold(
            onSuccess = { chanson ->
                audioPlayerManager.play(chanson.url)
            },
            onFailure = { error ->
                _uiState.value = ChansonUiState.Error(error.message)
            }
        )
    }
}
```

### **4. AudioPlayerManager joue l'audio**
```kotlin
// AudioPlayerManager.kt
fun play(url: String) {
    mediaPlayer.setDataSource(url)
    mediaPlayer.prepareAsync()
    mediaPlayer.setOnPreparedListener {
        mediaPlayer.start()
        _isPlaying.value = true      // Notifie les observers
    }
}
```

### **5. Fragment observe et met à jour l'UI**
```kotlin
// ChansonListFragment.kt
viewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
    val currentId = viewModel.currentPlayingId.value
    if (currentId != null) {
        adapter.updatePlayingState(currentId, isPlaying)
    }
}
```

### **6. Adapter change l'icône**
```kotlin
// ChansonAdapter.kt
when {
    isPlaying && !isPaused -> {
        binding.btnPlay.setImageResource(android.R.drawable.ic_media_pause)  // ⏸️
        binding.btnPlay.alpha = 1.0f
    }
    else -> {
        binding.btnPlay.setImageResource(android.R.drawable.ic_media_play)   // ▶️
        binding.btnPlay.alpha = 0.7f
    }
}
```

---

## ⏸️ Pause/Resume

### **Pause**
```kotlin
// User clique sur ⏸️
viewModel.pauseChanson() {
    audioPlayerManager.pause()
    mediaPlayer.pause()
    _isPlaying.value = false  → Notifie observers
}

// Fragment observe: isPlaying = false
adapter.updatePlayingState(chansonId, false)
// Adapter change l'icône en ▶️
```

### **Resume**
```kotlin
// User clique sur ▶️
viewModel.playChanson(sameChansonId) {
    // URL est déjà en mémoire
    audioPlayerManager.play(url)
}

// Fragment observe: isPlaying = true
adapter.updatePlayingState(chansonId, true)
// Adapter change l'icône en ⏸️
```

---

## 📊 État de l'Adapter

```
Propriétés:
├── currentPlayingId: Long?          // ID de la chanson en lecture
├── isCurrentlyPlaying: Boolean       // true=lecture, false=pause/stop
└── Chanson data: List<ChansonResponse>

Méthodes:
├── updatePlayingState(id, isPlaying)    // Sync avec ViewModel
├── pauseCurrentSong()                   // Passe isPaused=false
├── resumeCurrentSong()                  // Passe isPaused=true
└── updatePlayButtonIcon()               // Change ⏸️ ↔ ▶️
```

---

## 🔌 Connexion ViewModel-AudioPlayerManager

```kotlin
// ViewModel expose ces LiveData:
val isPlaying: LiveData<Boolean> = audioPlayerManager.isPlaying
val currentSongUrl: LiveData<String?> = audioPlayerManager.currentSongUrl
val playerError: LiveData<String?> = audioPlayerManager.error

// Fragment subscribe à ces observers
viewModel.isPlaying.observe() { ... }
viewModel.playerError.observe() { ... }
viewModel.currentPlayingId.observe() { ... }  // Tracking custom du ViewModel
```

---

## 🎨 États visuels du bouton

| État | Icône | Alpha | Action clique |
|------|-------|-------|---------------|
| Lecture | ⏸️ Pause | 1.0 | Appelle pause |
| Pause | ▶️ Play | 0.7 | Appelle play (resume) |
| Arrêt | ▶️ Play | 1.0 | Appelle play (new song) |

---

## 📋 Fichiers modifiés

### **ChansonAdapter.kt**
- ✅ Callbacks: `onPlayClick`, `onPauseClick`
- ✅ Propriété: `currentPlayingId`, `isCurrentlyPlaying`
- ✅ Méthodes: `updatePlayingState()`, `pauseCurrentSong()`, `resumeCurrentSong()`
- ✅ Fonction: `updatePlayButtonIcon()` pour changer ⏸️ ↔ ▶️

### **ChansonViewModel.kt**
- ✅ Import: `delay` de kotlinx.coroutines
- ✅ LiveData: `_currentPlayingId` pour tracker la chanson
- ✅ Fonctions: `playChanson()`, `pauseChanson()`, `resumeChanson()`, `stopChanson()`
- ✅ Logs détaillés pour le debugging

### **ChansonListFragment.kt**
- ✅ Passer 2 callbacks à l'adapter (play + pause)
- ✅ Observer: `isPlaying`, `currentPlayingId`, `playerError`
- ✅ Appel: `adapter.updatePlayingState()` pour sync

### **AudioPlayerManager.kt**
- ✅ Logs détaillés: setDataSource, prepareAsync, onPrepared
- ✅ Méthodes: `play()`, `pause()`, `resume()`, `stop()`, `release()`
- ✅ LiveData: `isPlaying`, `currentSongUrl`, `error`

### **AndroidManifest.xml**
- ✅ Permission: `android.permission.READ_EXTERNAL_STORAGE`

---

## 🧪 Test - Voir ça en action

**Logcat Filter**: `ChansonAdapter|AudioPlayerManager|ChansonViewModel`

1. **Cliquer Play**
```
ChansonAdapter: Play clicked for: Dont ever Leave me (ID: 1)
ChansonAdapter: Icon: PAUSE
AudioPlayerManager: === PLAY START ===
AudioPlayerManager: Setting data source
AudioPlayerManager: OnPreparedListener triggered
AudioPlayerManager: Song started playing successfully
```

2. **Cliquer Pause**
```
ChansonAdapter: Currently playing, pausing...
ChansonViewModel: pauseChanson called
AudioPlayerManager: Pausing song
AudioPlayerManager: Song paused successfully
ChansonAdapter: Icon: PLAY (paused)
```

3. **Cliquer Play à nouveau**
```
ChansonAdapter: Not playing or paused, playing...
AudioPlayerManager: Resuming song from: https://...
ChansonAdapter: Icon: PAUSE
```

---

## ✨ Pourquoi cette architecture?

### ❌ Mauvais (Fragment fait tout)
```kotlin
class ChansonListFragment : Fragment() {
    fun onPlayClick(chanson: ChansonResponse) {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(chanson.url)
        mediaPlayer.start()  // ❌ Bloque l'UI!
    }
}
```

### ✅ Bon (MVVM)
```
Fragment (observer uniquement)
    ↓ appelle
ViewModel (orchestre)
    ↓ appelle
AudioPlayerManager (exécute)
    ↓ notifie
LiveData
    ↓ observe
Fragment (met à jour)
```

**Avantages**:
- ✅ Fragment simple et testable
- ✅ ViewModel survit aux rotations
- ✅ Logique séparée en couches
- ✅ AudioPlayerManager réutilisable

---

## 🎯 Résumé rapide

```
┌─────────────────────────────────────────────┐
│ User clique Play/Pause dans RecyclerView   │
└─────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────┐
│ Adapter détecte le clic                     │
│ → onPlayClick() ou onPauseClick()           │
└─────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────┐
│ Fragment reçoit callback                    │
│ → viewModel.playChanson() ou pauseChanson() │
└─────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────┐
│ ViewModel orchestr/e                        │
│ → Repository.getChansonDetails()            │
│ → AudioPlayerManager.play() ou pause()      │
└─────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────┐
│ AudioPlayerManager exécute                  │
│ → MediaPlayer.start() ou pause()            │
│ → _isPlaying.value = true/false             │
└─────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────┐
│ Observer des LiveData notifie Fragment     │
│ → Fragment met à jour l'adapter             │
│ → Adapter change l'icône ⏸️ ou ▶️            │
└─────────────────────────────────────────────┘
                     ↓
            🎵 Musique joue! 🎵
```

---

C'est MVVM, c'est simple, c'est efficace! 🚀

