package com.isetr.smarttune.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.isetr.smarttune.databinding.ActivitySignupArtistBinding
import com.isetr.smarttune.viewModel.AuthUiState
import com.isetr.smarttune.viewModel.AuthViewModel
import java.io.File

class SignupArtistActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupArtistBinding
    private val viewModel: AuthViewModel by viewModels()
    private var pdfUri: Uri? = null

    private val pickPdf = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            pdfUri = it
            binding.tvPdfStatus.text = "✓ PDF sélectionné"
            binding.tvPdfStatus.setTextColor(resources.getColor(android.R.color.holo_green_light, null))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupArtistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Upload PDF
        binding.cardUpload.setOnClickListener {
            pickPdf.launch("application/pdf")
        }

        // Bouton Soumettre
        binding.btnSubmit.setOnClickListener {
            if (!validateForm()) return@setOnClickListener

            val pdfFile = pdfUri?.let { uriToFile(it) }
            if (pdfFile == null) {
                showError("Veuillez sélectionner un PDF de vérification")
                return@setOnClickListener
            }

            // Collecte des données
            val username = binding.etUsername.text.toString().trim()
            val nom = binding.etNom.text.toString().trim()
            val prenom = binding.etPrenom.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val numTel = binding.etNumTel.text.toString().trim().ifBlank { "" }
            val dateNaissance = binding.etDateNaissance.text.toString().trim()
            val genre = if (binding.rbFemme.isChecked) "F" else "H"
            val password = binding.etPassword.text.toString()
            val bio = binding.etBio.text.toString().trim()

            // Appel API
            viewModel.registerArtist(
                username = username,
                nom = nom,
                prenom = prenom,
                email = email,
                numTel = numTel,
                dateNaissance = dateNaissance,
                genre = genre,
                password = password,
                bio = bio,
                pdfFile = pdfFile
            )
        }
    }

    private fun validateForm(): Boolean {
        with(binding) {
            // Nom d'artiste
            val username = etUsername.text.toString().trim()
            if (username.isBlank()) {
                tilUsername.error = "Nom d'artiste requis"
                etUsername.requestFocus()
                return false
            }
            if (username.length < 3) {
                tilUsername.error = "Minimum 3 caractères"
                etUsername.requestFocus()
                return false
            }
            if (!username.matches(Regex("^[a-zA-Z0-9_-]+$"))) {
                tilUsername.error = "Caractères autorisés: lettres, chiffres, _, -"
                etUsername.requestFocus()
                return false
            }
            tilUsername.error = null

            // Nom
            val nom = etNom.text.toString().trim()
            if (nom.isBlank()) {
                tilNom.error = "Nom requis"
                etNom.requestFocus()
                return false
            }
            if (nom.length < 2) {
                tilNom.error = "Minimum 2 caractères"
                etNom.requestFocus()
                return false
            }
            tilNom.error = null

            // Prénom
            val prenom = etPrenom.text.toString().trim()
            if (prenom.isBlank()) {
                tilPrenom.error = "Prénom requis"
                etPrenom.requestFocus()
                return false
            }
            if (prenom.length < 2) {
                tilPrenom.error = "Minimum 2 caractères"
                etPrenom.requestFocus()
                return false
            }
            tilPrenom.error = null

            // Email
            val email = etEmail.text.toString().trim()
            if (email.isBlank()) {
                tilEmail.error = "Email requis"
                etEmail.requestFocus()
                return false
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Email invalide"
                etEmail.requestFocus()
                return false
            }
            tilEmail.error = null

            // Numéro téléphone (optionnel mais si rempli, doit être valide)
            val numTel = etNumTel.text.toString().trim()
            if (numTel.isNotBlank()) {
                if (!numTel.matches(Regex("^(\\+216|00216)?[0-9]{8}$"))) {
                    tilNumTel.error = "Téléphone tunisien invalide"
                    etNumTel.requestFocus()
                    return false
                }
            }
            tilNumTel.error = null

            // Date de naissance
            val dateNaissance = etDateNaissance.text.toString().trim()
            if (dateNaissance.isBlank()) {
                tilDateNaissance.error = "Date de naissance requise"
                etDateNaissance.requestFocus()
                return false
            }
            tilDateNaissance.error = null

            // Genre
            val genre = if (binding.rbFemme.isChecked) "F" else "H"
            if (genre !in listOf("H", "F")) {
                showError("Genre invalide")
                return false
            }

            // Mot de passe
            val password = etPassword.text.toString()
            if (password.length < 8) {
                tilPassword.error = "Minimum 8 caractères"
                etPassword.requestFocus()
                return false
            }
            if (!password.matches(Regex(".*[A-Z].*"))) {
                tilPassword.error = "Au moins 1 majuscule"
                etPassword.requestFocus()
                return false
            }
            if (!password.matches(Regex(".*[a-z].*"))) {
                tilPassword.error = "Au moins 1 minuscule"
                etPassword.requestFocus()
                return false
            }
            if (!password.matches(Regex(".*[0-9].*"))) {
                tilPassword.error = "Au moins 1 chiffre"
                etPassword.requestFocus()
                return false
            }
            if (!password.matches(Regex(".*[^A-Za-z0-9].*"))) {
                tilPassword.error = "Au moins 1 caractère spécial"
                etPassword.requestFocus()
                return false
            }
            tilPassword.error = null

            // Bio
            val bio = etBio.text.toString().trim()
            if (bio.length < 50) {
                tilBio.error = "Minimum 50 caractères (${bio.length}/50)"
                etBio.requestFocus()
                return false
            }
            if (bio.length > 1000) {
                tilBio.error = "Maximum 1000 caractères"
                etBio.requestFocus()
                return false
            }
            tilBio.error = null

            // PDF
            if (pdfUri == null) {
                showError("Veuillez sélectionner un PDF de vérification")
                return false
            }
        }

        return true
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is AuthUiState.Loading -> {
                    setLoadingState(true)
                }
                is AuthUiState.SuccessArtist -> {
                    setLoadingState(false)
                    showSuccess("Demande envoyée avec succès !\nVous recevrez un email après validation.\nConnectez-vous pour continuer.")

                    // Navigation après 2 secondes
                    binding.root.postDelayed({
                        val intent = Intent(this@SignupArtistActivity, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    }, 2000)
                }
                is AuthUiState.Error -> {
                    setLoadingState(false)
                    showError(state.message)
                }
                else -> {
                    setLoadingState(false)
                }
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmit.isEnabled = !isLoading
        binding.btnSubmit.alpha = if (isLoading) 0.5f else 1.0f

        // Désactiver les champs pendant le chargement
        binding.etUsername.isEnabled = !isLoading
        binding.etNom.isEnabled = !isLoading
        binding.etPrenom.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etNumTel.isEnabled = !isLoading
        binding.etDateNaissance.isEnabled = !isLoading
        binding.rgGenre.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.etBio.isEnabled = !isLoading
        binding.cardUpload.isClickable = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun onReturnClick(view: android.view.View) {
        finish()
    }


    private fun uriToFile(uri: Uri): File? {
        return try {
            val fileName = "artist_verif_${System.currentTimeMillis()}.pdf"
            val file = File(cacheDir, fileName)

            contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (file.exists() && file.length() > 0) {
                file
            } else {
                showError("Erreur lors de la lecture du fichier PDF")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Erreur lors du traitement du PDF: ${e.message}")
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Nettoyer les fichiers temporaires
        cacheDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("artist_verif_")) {
                file.delete()
            }
        }
    }
}