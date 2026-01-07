package com.isetr.smarttune.ui.artist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.isetr.smarttune.databinding.ActivityArtistDashboardBinding

class ArtistDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArtistDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtistDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupRecyclerView()
    }

    private fun setupViews() {
        binding.fabUpload.setOnClickListener {
            // TODO: Ouvrir dialog ou nouvelle activité pour upload
        }
    }

    private fun setupRecyclerView() {
        binding.rvMyMusic.layoutManager = LinearLayoutManager(this)
        // TODO: Ajouter l'adaptateur avec les données de l'artiste
    }
}