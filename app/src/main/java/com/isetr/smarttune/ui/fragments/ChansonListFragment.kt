package com.isetr.smarttune.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isetr.smarttune.databinding.FragmentChansonListBinding
import com.isetr.smarttune.ui.adapter.ChansonAdapter
import com.isetr.smarttune.viewModel.ChansonUiState
import com.isetr.smarttune.viewModel.ChansonViewModel
import com.isetr.smarttune.viewModel.ChansonViewModelFactory

class ChansonListFragment : Fragment() {

    private lateinit var binding: FragmentChansonListBinding
    private lateinit var viewModel: ChansonViewModel
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

        // Initialiser le ViewModel avec ViewModelFactory
        val factory = ChansonViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory).get(ChansonViewModel::class.java)

        // Initialiser l'adapter
        adapter = ChansonAdapter { chanson ->
            Toast.makeText(
                requireContext(),
                "Lecture: ${chanson.titre}",
                Toast.LENGTH_SHORT
            ).show()
            // Plus tard: implémenter la logique de lecture
        }

        // Configurer le RecyclerView
        binding.rvChansons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChansonListFragment.adapter
            setPadding(20, 0, 20, 16)
            clipToPadding = false
        }

        // Observer l'état du ViewModel
        observeViewModel()

        // Charger les chansons aléatoires au démarrage
        viewModel.loadRandomChansons("sad")
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
}

