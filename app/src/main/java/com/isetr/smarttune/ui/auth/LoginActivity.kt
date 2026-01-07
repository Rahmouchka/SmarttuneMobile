package com.isetr.smarttune.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.isetr.smarttune.databinding.ActivityLoginBinding
import com.isetr.smarttune.ui.artist.ArtistDashboardActivity
import com.isetr.smarttune.ui.user.UserHomeActivity
import com.isetr.smarttune.viewModel.AuthUiState
import com.isetr.smarttune.viewModel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeAuthState()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.tilEmail.editText?.text.toString().trim()
            val password = binding.tilPassword.editText?.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                showError("Veuillez remplir tous les champs")
                return@setOnClickListener
            }

            hideError()
            viewModel.login(email, password)
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupTypeActivity::class.java))
        }
    }

    private fun observeAuthState() {
        viewModel.uiState.observe(this) { state ->
            try {
                when (state) {
                    is AuthUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnLogin.isEnabled = false
                        hideError()
                    }

                    is AuthUiState.SuccessUser -> {
                        binding.progressBar.visibility = View.GONE

                        val user = state.user
                        Log.e("LOGIN_DEBUG", "User reçu → $user")

                        // On affiche TOUT dans les logs
                        Log.e("LOGIN_DEBUG", "role = '${user.role}' | userType = '${user.userType}'")

                        // Détection ultra-robuste du rôle
                        val roleText = user.role?.trim()?.uppercase()
                            ?: user.userType?.trim()?.uppercase()
                            ?: "USER"

                        Log.e("LOGIN_DEBUG", "Rôle final détecté → $roleText")

                        val target = if (roleText.contains("ARTIST")) {
                            ArtistDashboardActivity::class.java
                        } else {
                            UserHomeActivity::class.java
                        }

                        Toast.makeText(this@LoginActivity, "Connexion réussie → $roleText", Toast.LENGTH_LONG).show()

                        startActivity(Intent(this@LoginActivity, target))
                        finish()
                    }

                    is AuthUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        showError(state.message)
                    }

                    else -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                // ATTRAPE TOUT CRASH ICI
                Log.e("LOGIN_CRASH", "Crash dans observeAuthState", e)
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                showError("Erreur technique : ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = message
    }

    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }
}