package com.isetr.smarttune.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.isetr.smarttune.databinding.ActivitySignupUserBinding
import com.isetr.smarttune.viewModel.AuthUiState
import com.isetr.smarttune.viewModel.AuthViewModel

class SignupUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupUserBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCreateAccount.setOnClickListener {
            if (!validateForm()) return@setOnClickListener

            val dateStr = binding.etDateNaissance.text.toString().trim()
            val genre = if (binding.rbFemme.isChecked) "F" else "H"

            viewModel.registerUser(
                username = binding.etUsername.text.toString().trim(),
                nom = binding.etNom.text.toString().trim(),
                prenom = binding.etPrenom.text.toString().trim(),
                email = binding.etEmail.text.toString().trim(),
                numTel = binding.etNumTel.text.toString().trim().ifBlank { "" },
                dateNaissance = dateStr,
                genre = genre,
                password = binding.etPassword.text.toString()
            )
        }

        binding.tvLogin.setOnClickListener { finish() }

        observeViewModel()
    }

    fun onReturnClick(view: android.view.View) {
        finish()
    }

    private fun validateForm(): Boolean {
        val username = binding.etUsername.text.toString().trim()
        val nom = binding.etNom.text.toString().trim()
        val prenom = binding.etPrenom.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val dateNaissance = binding.etDateNaissance.text.toString().trim()
        val numTel = binding.etNumTel.text.toString().trim()

        when {
            username.isBlank() -> showError("Nom d'utilisateur requis")
            username.length < 3 -> showError("Nom d'utilisateur minimum 3 caractères")
            nom.isBlank() -> showError("Nom requis")
            nom.length < 2 -> showError("Nom minimum 2 caractères")
            prenom.isBlank() -> showError("Prénom requis")
            prenom.length < 2 -> showError("Prénom minimum 2 caractères")
            email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> showError("Email invalide")
            dateNaissance.isBlank() -> showError("Date de naissance requise")
            password.length < 8 -> showError("Mot de passe minimum 8 caractères")
            !password.matches(Regex(".*[A-Z].*")) -> showError("Mot de passe: 1 majuscule requise")
            !password.matches(Regex(".*[a-z].*")) -> showError("Mot de passe: 1 minuscule requise")
            !password.matches(Regex(".*[0-9].*")) -> showError("Mot de passe: 1 chiffre requis")
            !password.matches(Regex(".*[^A-Za-z0-9].*")) -> showError("Mot de passe: 1 caractère spécial requis")
            password != confirmPassword -> showError("Les mots de passe ne correspondent pas")
            else -> return true
        }
        return false
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is AuthUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnCreateAccount.isEnabled = false
                    binding.tvError.visibility = View.GONE
                }
                is AuthUiState.SuccessUser -> {
                    Toast.makeText(this@SignupUserActivity, "Compte créé avec succès ! Connectez-vous.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@SignupUserActivity, LoginActivity::class.java))
                    finish()
                }
                is AuthUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCreateAccount.isEnabled = true
                    showError(state.message)
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCreateAccount.isEnabled = true
                }
            }
        }
    }
}