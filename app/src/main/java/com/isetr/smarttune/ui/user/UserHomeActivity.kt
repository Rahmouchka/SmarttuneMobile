package com.isetr.smarttune.ui.user

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.isetr.smarttune.R
import com.isetr.smarttune.databinding.ActivityUserHomeBinding
import com.isetr.smarttune.ui.auth.LoginActivity
import com.isetr.smarttune.ui.fragments.ChansonListFragment
import com.isetr.smarttune.ui.fragments.FavorisFragment
import com.isetr.smarttune.ui.fragments.PlaylistsFragment
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
        setupNavigation()

        // Mettre le focus sur Accueil par défaut
        updateNavSelection("home")
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
            // TODO: Implémenter la recherche avancée
        }
    }

    private fun setupNavigation() {
        // Accueil
        binding.navHome.setOnClickListener {
            showFragment(ChansonListFragment(), "🔥 Tendances")
            updateNavSelection("home")
        }

        // Favoris
        binding.navFavoris.setOnClickListener {
            showFragment(FavorisFragment(), "❤️ Mes Favoris")
            updateNavSelection("favoris")
        }

        // Playlists
        binding.navPlaylists.setOnClickListener {
            showFragment(PlaylistsFragment(), "🎵 Mes Playlists")
            updateNavSelection("playlists")
        }

        // Profil
        binding.navProfile.setOnClickListener {
            updateNavSelection("profile")
            // TODO: Implémenter le profil
        }
    }

    private fun showFragment(fragment: Fragment, title: String) {
        binding.tvPopularTitle.text = title
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_chansons, fragment)
            .commit()
    }

    private fun updateNavSelection(selected: String) {
        val activeColor = 0xFFFF6B6B.toInt()
        val inactiveColor = 0xFF888888.toInt()

        // Liste des boutons de navigation
        val navItems = listOf(
            Triple(binding.navHome, "home", R.drawable.ic_home),
            Triple(binding.navFavoris, "favoris", R.drawable.ic_favorite),
            Triple(binding.navPlaylists, "playlists", R.drawable.ic_library),
            Triple(binding.navProfile, "profile", R.drawable.ic_profile)
        )

        for ((navLayout, id, iconRes) in navItems) {
            val isActive = id == selected
            val color = if (isActive) activeColor else inactiveColor

            // Changer la couleur de l'icône
            val imageView = navLayout.getChildAt(0) as? ImageView
            imageView?.setColorFilter(color)

            // Changer la couleur du texte
            val textView = navLayout.getChildAt(1) as? TextView
            textView?.setTextColor(color)
        }
    }
}