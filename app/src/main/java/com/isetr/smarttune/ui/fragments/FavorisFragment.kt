package com.isetr.smarttune.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isetr.smarttune.databinding.FragmentFavorisBinding
import com.isetr.smarttune.ui.adapter.FavorisAdapter
import com.isetr.smarttune.viewModel.ChansonViewModel
import com.isetr.smarttune.viewModel.ChansonViewModelFactory
import com.isetr.smarttune.viewModel.FavorisUiState
import com.isetr.smarttune.viewModel.FavorisViewModel
import com.isetr.smarttune.viewModel.FavorisViewModelFactory

class FavorisFragment : Fragment() {

    private lateinit var binding: FragmentFavorisBinding
    private lateinit var viewModel: FavorisViewModel
    private lateinit var chansonViewModel: ChansonViewModel
    private lateinit var adapter: FavorisAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavorisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser les ViewModels avec ViewModelFactory
        val factory = FavorisViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory).get(FavorisViewModel::class.java)

        val chansonFactory = ChansonViewModelFactory(requireActivity().application)
        chansonViewModel = ViewModelProvider(this, chansonFactory).get(ChansonViewModel::class.java)

        // Initialiser l'adapter avec play, pause et remove
        adapter = FavorisAdapter(
            onPlayClick = { chanson ->
                chansonViewModel.playChanson(chanson.id)
            },
            onPauseClick = { chanson ->
                chansonViewModel.pauseChanson()
            },
            onRemoveClick = { chanson ->
                viewModel.removeFromFavoris(chanson.id)
            }
        )

        // Configurer le RecyclerView
        binding.rvFavoris.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FavorisFragment.adapter
        }

        // Observer l'état du ViewModel
        observeViewModel()

        // Observer l'état de lecture
        chansonViewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            val currentId = chansonViewModel.currentPlayingId.value
            if (currentId != null) {
                adapter.updatePlayingState(currentId, isPlaying)
            }
            if (isPlaying) {
                Toast.makeText(requireContext(), "Lecture en cours...", Toast.LENGTH_SHORT).show()
            }
        }

        chansonViewModel.currentPlayingId.observe(viewLifecycleOwner) { chansonId ->
            val isPlaying = chansonViewModel.isPlaying.value ?: false
            adapter.updatePlayingState(chansonId, isPlaying)
        }

        chansonViewModel.playerError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), "Erreur audio: $error", Toast.LENGTH_LONG).show()
            }
        }

        // Charger les favoris
        viewModel.loadFavoris()
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                FavorisUiState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                }
                FavorisUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvFavoris.visibility = View.GONE
                    binding.tvEmpty.visibility = View.GONE
                }
                is FavorisUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.favoris.isEmpty()) {
                        binding.rvFavoris.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        binding.rvFavoris.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
                        adapter.setFavoris(state.favoris)
                    }
                }
                is FavorisUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvFavoris.visibility = View.GONE
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
}

