package com.isetr.smarttune.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isetr.smarttune.data.dto.ChansonResponse
import com.isetr.smarttune.databinding.FragmentChansonListBinding
import com.isetr.smarttune.ui.adapter.ChansonAdapter
import com.isetr.smarttune.viewModel.ChansonUiState
import com.isetr.smarttune.viewModel.ChansonViewModel
import com.isetr.smarttune.viewModel.ChansonViewModelFactory
import com.isetr.smarttune.viewModel.FavorisViewModel
import com.isetr.smarttune.viewModel.FavorisViewModelFactory
import com.isetr.smarttune.viewModel.PlaylistViewModel
import com.isetr.smarttune.viewModel.PlaylistViewModelFactory
import com.isetr.smarttune.viewModel.PlaylistUiState

class ChansonListFragment : Fragment() {

    private lateinit var binding: FragmentChansonListBinding
    private lateinit var viewModel: ChansonViewModel
    private lateinit var favorisViewModel: FavorisViewModel
    private lateinit var playlistViewModel: PlaylistViewModel
    private lateinit var adapter: ChansonAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChansonListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser les ViewModels avec ViewModelFactory
        val factory = ChansonViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory).get(ChansonViewModel::class.java)

        val favorisFactory = FavorisViewModelFactory(requireActivity().application)
        favorisViewModel = ViewModelProvider(this, favorisFactory).get(FavorisViewModel::class.java)

        val playlistFactory = PlaylistViewModelFactory(requireActivity().application)
        playlistViewModel = ViewModelProvider(this, playlistFactory).get(PlaylistViewModel::class.java)

        // Initialiser l'adapter avec callbacks pour play, pause, favoris et playlist
        adapter = ChansonAdapter(
            onPlayClick = { chanson ->
                android.util.Log.d("ChansonListFragment", "Play clicked")
                viewModel.playChanson(chanson.id)
            },
            onPauseClick = { chanson ->
                android.util.Log.d("ChansonListFragment", "Pause clicked")
                viewModel.pauseChanson()
            },
            onFavoriteClick = { chanson ->
                android.util.Log.d("ChansonListFragment", "Favorite clicked for: ${chanson.titre}")
                favorisViewModel.toggleFavoris(chanson.id)
            },
            onAddToPlaylistClick = { chanson ->
                android.util.Log.d("ChansonListFragment", "Add to playlist clicked for: ${chanson.titre}")
                showAddToPlaylistDialog(chanson)
            }
        )

        // Configurer le RecyclerView
        binding.rvChansons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChansonListFragment.adapter
            setPadding(20, 0, 20, 16)
            clipToPadding = false
        }

        // Observer l'état du ViewModel
        observeViewModel()

        // Observer les playlists
        observePlaylistViewModel()

        // Observer l'état de lecture pour afficher le feedback
        viewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            android.util.Log.d("ChansonListFragment", "isPlaying changed: $isPlaying")
            // Mettre à jour l'adapter avec le nouvel état
            val currentId = viewModel.currentPlayingId.value
            if (currentId != null) {
                adapter.updatePlayingState(currentId, isPlaying)
            }
            if (isPlaying) {
                Toast.makeText(requireContext(), "Lecture en cours...", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.playerError.observe(viewLifecycleOwner) { error ->
            android.util.Log.d("ChansonListFragment", "playerError: $error")
            if (error != null) {
                Toast.makeText(requireContext(), "Erreur audio: $error", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.currentSongUrl.observe(viewLifecycleOwner) { url ->
            android.util.Log.d("ChansonListFragment", "currentSongUrl: $url")
        }

        // Observer le chanson ID en lecture
        viewModel.currentPlayingId.observe(viewLifecycleOwner) { chansonId ->
            android.util.Log.d("ChansonListFragment", "currentPlayingId changed: $chansonId")
            val isPlaying = viewModel.isPlaying.value ?: false
            if (chansonId != null) {
                adapter.updatePlayingState(chansonId, isPlaying)
            } else {
                adapter.updatePlayingState(null, false)
            }
        }

        // Charger toutes les chansons au démarrage (avec query vide comme dans Postman)
        viewModel.searchChansons("")

        // Charger les favoris pour afficher les cœurs remplis
        favorisViewModel.loadFavoris()

        // Observer les favoris
        favorisViewModel.favorisIds.observe(viewLifecycleOwner) { ids ->
            adapter.updateFavorites(ids)
        }

        favorisViewModel.operationSuccess.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                favorisViewModel.clearMessages()
            }
        }

        favorisViewModel.operationError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                favorisViewModel.clearMessages()
            }
        }

        // Ajouter la barre de recherche depuis l'activity parent
        setupSearchBar()
    }

    private fun setupSearchBar() {
        // La barre de recherche est dans l'Activity parent (UserHomeActivity)
        // On peut y accéder via binding si elle existe
        try {
            val parentActivity = requireActivity() as? com.isetr.smarttune.ui.user.UserHomeActivity
            // Chercher la barre de recherche dans l'activity parent
            val searchEditText = requireActivity().findViewById<android.widget.EditText>(
                com.isetr.smarttune.R.id.et_search
            )

            if (searchEditText != null) {
                searchEditText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        val query = s?.toString()?.trim() ?: ""
                        // Envoyer la query telle quelle (vide pour toutes les chansons)
                        viewModel.searchChansons(query)
                    }
                })
            }
        } catch (e: Exception) {
            android.util.Log.d("ChansonListFragment", "SearchBar not found in parent activity")
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            android.util.Log.d("ChansonListFragment", "State changed: $state")
            when (state) {
                ChansonUiState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvChansons.visibility = View.VISIBLE
                }
                ChansonUiState.Loading -> {
                    android.util.Log.d("ChansonListFragment", "Loading...")
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvChansons.visibility = View.GONE
                }
                is ChansonUiState.Success -> {
                    android.util.Log.d("ChansonListFragment", "Success! Chansons count: ${state.chansons.size}")
                    binding.progressBar.visibility = View.GONE
                    binding.rvChansons.visibility = View.VISIBLE
                    adapter.setChansons(state.chansons)
                    android.util.Log.d("ChansonListFragment", "Adapter updated with ${state.chansons.size} items")
                }
                is ChansonUiState.Error -> {
                    android.util.Log.d("ChansonListFragment", "Error: ${state.message}")
                    binding.progressBar.visibility = View.GONE
                    binding.rvChansons.visibility = View.VISIBLE
                    Toast.makeText(
                        requireContext(),
                        "Erreur: ${state.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun observePlaylistViewModel() {
        playlistViewModel.operationSuccess.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                playlistViewModel.clearMessages()
            }
        }

        playlistViewModel.operationError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                playlistViewModel.clearMessages()
            }
        }

        // Charger les playlists au démarrage
        playlistViewModel.loadPlaylists()
    }

    private fun showAddToPlaylistDialog(chanson: ChansonResponse) {
        val state = playlistViewModel.uiState.value

        if (state is PlaylistUiState.Success && state.playlists.isNotEmpty()) {
            // Si des playlists existent, afficher le choix
            val playlistNames = mutableListOf("➕ Créer nouvelle playlist")
            playlistNames.addAll(state.playlists.map { it.titre })

            AlertDialog.Builder(requireContext())
                .setTitle("Ajouter \"${chanson.titre}\" à...")
                .setItems(playlistNames.toTypedArray()) { _, which ->
                    if (which == 0) {
                        // Créer nouvelle playlist
                        showCreatePlaylistAndAddSongDialog(chanson)
                    } else {
                        // Ajouter à une playlist existante
                        val selectedPlaylist = state.playlists[which - 1]
                        playlistViewModel.addChansonsToPlaylist(selectedPlaylist.id, listOf(chanson.id))
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        } else {
            // Aucune playlist, proposer d'en créer une
            showCreatePlaylistAndAddSongDialog(chanson)
        }
    }

    private fun showCreatePlaylistAndAddSongDialog(chanson: ChansonResponse) {
        val editText = EditText(requireContext()).apply {
            hint = "Nom de la playlist"
            setPadding(50, 30, 50, 30)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Nouvelle Playlist")
            .setMessage("Créer une playlist pour \"${chanson.titre}\"")
            .setView(editText)
            .setPositiveButton("Créer") { _, _ ->
                val titre = editText.text.toString().trim()
                if (titre.isNotEmpty()) {
                    // Créer la playlist puis y ajouter la chanson
                    playlistViewModel.createPlaylistAndAddSong(titre, chanson.id)
                } else {
                    Toast.makeText(requireContext(), "Le nom ne peut pas être vide", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}

