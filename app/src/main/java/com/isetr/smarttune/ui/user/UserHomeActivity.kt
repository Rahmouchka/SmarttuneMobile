package com.isetr.smarttune.ui.user

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isetr.smarttune.databinding.ActivityUserHomeBinding
import com.isetr.smarttune.ui.auth.LoginActivity
import com.isetr.smarttune.viewModel.AuthViewModel

class UserHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserHomeBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        setupHeader()
        setupSearchBar()
        setupRecyclerView()
    }

    private fun setupHeader() {
        // Observer la session courante pour afficher le username
        authViewModel.authenticatedUser.observe(this) { session ->
            if (session != null) {
                binding.tvUsername.text = session.username
            }
        }

        // Bouton de déconnexion
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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