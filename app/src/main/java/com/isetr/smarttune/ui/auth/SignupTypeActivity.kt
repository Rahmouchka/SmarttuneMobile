package com.isetr.smarttune.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.isetr.smarttune.databinding.ActivitySignupTypeBinding

class SignupTypeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupTypeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Les bons boutons avec les bons IDs
        binding.btnUser.setOnClickListener {
            startActivity(Intent(this, SignupUserActivity::class.java))
        }

        binding.btnArtist.setOnClickListener {
            startActivity(Intent(this, SignupArtistActivity::class.java))
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}