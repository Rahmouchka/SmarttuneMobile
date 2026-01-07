// MainActivity.kt
package com.isetr.smarttune.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.isetr.smarttune.R
import com.isetr.smarttune.databinding.ActivityMainBinding
import com.isetr.smarttune.ui.auth.SignupTypeActivity
import com.isetr.smarttune.viewModel.AuthViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AuthViewModel by viewModels()
    private var isShowingAbout = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Toujours afficher activity_main en premier
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observer la session pour contrôler la navigation
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModel.authenticatedUser.observe(this, Observer { session ->
            if (session != null) {
                // UTILISATEUR CONNECTÉ
                binding.tvTitle.text = "Bienvenue,\nUtilisateur !"
                binding.tvSubtitle.text = "Ta musique t'attend partout, tout le temps."

                binding.btnStart.text = "Découvrir la musique"
                binding.btnStart.setOnClickListener {
                    Toast.makeText(this, "Feed en cours de développement", Toast.LENGTH_SHORT).show()
                    // Plus tard : ouvrir HomeFragment, Player, etc.
                }

                binding.btnMore.text = "Se déconnecter"
                binding.btnMore.setOnClickListener {
                    viewModel.logout()
                    // authenticatedUser devient null → l'observer redessine l'écran
                }
            } else {
                // PAS CONNECTÉ → Onboarding
                if (!isShowingAbout) {
                    binding.tvTitle.text = "Votre musique,\nPartout, Toujours"
                    binding.tvSubtitle.text = "Écoutez, découvrez et partagez avec une communauté passionnée."

                    binding.btnStart.text = "Commencer maintenant"
                    binding.btnStart.setOnClickListener {
                        startActivity(Intent(this, SignupTypeActivity::class.java))
                    }

                    binding.btnMore.text = "En savoir plus"
                    binding.btnMore.setOnClickListener {
                        showAboutSection()
                    }
                }
            }
        })
    }

    private fun showAboutSection() {
        isShowingAbout = true
        setContentView(R.layout.activity_main_about)

        // Récupérer les nouveaux views
        val tvBack = findViewById<android.widget.TextView>(R.id.tv_back)
        val btnCreateAccount = findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_create_account)

        // Bouton retour
        tvBack.setOnClickListener {
            isShowingAbout = false
            setContentView(binding.root)
            observeAuthState() // Re-observer pour l'état actuel
        }

        // Bouton créer un compte
        btnCreateAccount.setOnClickListener {
            startActivity(Intent(this, SignupTypeActivity::class.java))
        }
    }
}