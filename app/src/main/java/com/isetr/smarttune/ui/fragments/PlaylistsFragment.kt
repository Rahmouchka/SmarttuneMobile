package com.isetr.smarttune.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isetr.smarttune.R
import com.isetr.smarttune.databinding.FragmentPlaylistsBinding
import com.isetr.smarttune.databinding.DialogPlaylistDetailsBinding
import com.isetr.smarttune.data.dto.PlaylistResponse
import com.isetr.smarttune.data.dto.ChansonSimple
import com.isetr.smarttune.ui.adapter.PlaylistAdapter
import com.isetr.smarttune.ui.adapter.PlaylistSongAdapter
import com.isetr.smarttune.viewModel.ChansonViewModel
import com.isetr.smarttune.viewModel.ChansonViewModelFactory
import com.isetr.smarttune.viewModel.PlaylistUiState
import com.isetr.smarttune.viewModel.PlaylistViewModel
import com.isetr.smarttune.viewModel.PlaylistViewModelFactory

class PlaylistsFragment : Fragment() {

    private lateinit var binding: FragmentPlaylistsBinding
    private lateinit var viewModel: PlaylistViewModel
    private lateinit var chansonViewModel: ChansonViewModel
    private lateinit var adapter: PlaylistAdapter
    private var currentPlaylistDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser les ViewModels avec ViewModelFactory
        val factory = PlaylistViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory).get(PlaylistViewModel::class.java)

        val chansonFactory = ChansonViewModelFactory(requireActivity().application)
        chansonViewModel = ViewModelProvider(this, chansonFactory).get(ChansonViewModel::class.java)

        // Initialiser l'adapter
        adapter = PlaylistAdapter(
            onPlaylistClick = { playlist ->
                showPlaylistDetails(playlist)
            },
            onMoreClick = { playlist ->
                showPlaylistOptions(playlist)
            }
        )

        // Configurer le RecyclerView
        binding.rvPlaylists.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PlaylistsFragment.adapter
        }

        // Bouton ajouter playlist
        binding.btnAddPlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }

        // Observer l'état du ViewModel
        observeViewModel()

        // Charger les playlists
        viewModel.loadPlaylists()
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                PlaylistUiState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                }
                PlaylistUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvPlaylists.visibility = View.GONE
                    binding.tvEmpty.visibility = View.GONE
                }
                is PlaylistUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.playlists.isEmpty()) {
                        binding.rvPlaylists.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        binding.rvPlaylists.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
                        adapter.setPlaylists(state.playlists)
                    }
                }
                is PlaylistUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvPlaylists.visibility = View.GONE
                    binding.tvEmpty.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.operationSuccess.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }

        viewModel.operationError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearMessages()
            }
        }
    }

    private fun showCreatePlaylistDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "Nom de la playlist"
            setPadding(50, 30, 50, 30)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Nouvelle Playlist")
            .setView(editText)
            .setPositiveButton("Créer") { _, _ ->
                val titre = editText.text.toString().trim()
                if (titre.isNotEmpty()) {
                    viewModel.createPlaylist(titre)
                } else {
                    Toast.makeText(requireContext(), "Le nom ne peut pas être vide", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showPlaylistDetails(playlist: PlaylistResponse) {
        val dialogBinding = DialogPlaylistDetailsBinding.inflate(layoutInflater)

        dialogBinding.tvPlaylistName.text = playlist.titre
        val count = playlist.chansons?.size ?: 0
        dialogBinding.tvSongCount.text = "$count chanson${if (count > 1) "s" else ""}"

        val songAdapter = PlaylistSongAdapter(
            onPlayClick = { chanson ->
                chansonViewModel.playChanson(chanson.id)
                Toast.makeText(requireContext(), "Lecture: ${chanson.titre}", Toast.LENGTH_SHORT).show()
            },
            onRemoveClick = { chanson ->
                confirmRemoveSongFromPlaylist(playlist, chanson)
            }
        )

        dialogBinding.rvSongs.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }

        if (playlist.chansons.isNullOrEmpty()) {
            dialogBinding.tvEmpty.visibility = View.VISIBLE
            dialogBinding.rvSongs.visibility = View.GONE
        } else {
            dialogBinding.tvEmpty.visibility = View.GONE
            dialogBinding.rvSongs.visibility = View.VISIBLE
            songAdapter.setSongs(playlist.chansons)
        }

        currentPlaylistDialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnBack.setOnClickListener {
            currentPlaylistDialog?.dismiss()
        }

        currentPlaylistDialog?.show()
    }

    private fun confirmRemoveSongFromPlaylist(playlist: PlaylistResponse, chanson: ChansonSimple) {
        AlertDialog.Builder(requireContext())
            .setTitle("Retirer de la playlist")
            .setMessage("Voulez-vous retirer \"${chanson.titre}\" de \"${playlist.titre}\" ?")
            .setPositiveButton("Retirer") { _, _ ->
                viewModel.removeChansonFromPlaylist(playlist.id, chanson.id)
                currentPlaylistDialog?.dismiss()
                // Recharger pour rafraîchir
                viewModel.loadPlaylists()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showPlaylistOptions(playlist: PlaylistResponse) {
        val options = arrayOf("Modifier", "Supprimer")

        AlertDialog.Builder(requireContext())
            .setTitle(playlist.titre)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditPlaylistDialog(playlist)
                    1 -> confirmDeletePlaylist(playlist)
                }
            }
            .show()
    }

    private fun showEditPlaylistDialog(playlist: PlaylistResponse) {
        val editText = EditText(requireContext()).apply {
            setText(playlist.titre)
            setPadding(50, 30, 50, 30)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Modifier Playlist")
            .setView(editText)
            .setPositiveButton("Enregistrer") { _, _ ->
                val titre = editText.text.toString().trim()
                if (titre.isNotEmpty()) {
                    viewModel.updatePlaylist(playlist.id, titre, playlist.visible)
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun confirmDeletePlaylist(playlist: PlaylistResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Supprimer")
            .setMessage("Voulez-vous vraiment supprimer \"${playlist.titre}\" ?")
            .setPositiveButton("Supprimer") { _, _ ->
                viewModel.deletePlaylist(playlist.id)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}

