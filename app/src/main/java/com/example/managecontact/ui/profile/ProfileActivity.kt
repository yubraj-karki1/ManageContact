package com.example.managecontact.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.managecontact.R
import com.example.managecontact.databinding.ActivityProfileBinding
import com.example.managecontact.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        setupToolbar()
        setupClickListeners()
        loadUserProfile()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnEditProfile.setOnClickListener {
            // TODO: Implement edit profile functionality
            Toast.makeText(this, "Edit profile functionality coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun loadUserProfile() {
        showLoading(true)
        auth.currentUser?.let { user ->
            binding.tvName.text = user.displayName
            binding.tvEmail.text = user.email
            showLoading(false)
        } ?: run {
            showLoading(false)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnEditProfile.isEnabled = !show
        binding.btnLogout.isEnabled = !show
    }
} 