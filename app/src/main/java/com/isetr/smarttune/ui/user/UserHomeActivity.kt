package com.isetr.smarttune.ui.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.isetr.smarttune.databinding.ActivityUserHomeBinding

class UserHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSearchBar()
        setupRecyclerView()
    }

    private fun setupSearchBar() {
        binding.etSearch.setOnClickListener {
            // TODO: Implémenter la recherche
        }
    }

    private fun setupRecyclerView() {
        binding.rvMusic.layoutManager = LinearLayoutManager(this)
        // TODO: Ajouter l'adaptateur avec les données
    }
}