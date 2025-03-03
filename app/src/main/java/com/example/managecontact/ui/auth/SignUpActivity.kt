package com.example.managecontact.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.managecontact.R
import com.example.managecontact.databinding.ActivitySignUpBinding
import com.example.managecontact.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInput(fullName, email, password, confirmPassword)) {
                createAccount(fullName, email, password)
            }
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (fullName.isEmpty()) {
            binding.tilFullName.error = getString(R.string.error_field_required)
            Toast.makeText(this, getString(R.string.error_field_required), Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_field_required)
            Toast.makeText(this, getString(R.string.error_field_required), Toast.LENGTH_SHORT).show()
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            Toast.makeText(this, getString(R.string.error_invalid_email), Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_field_required)
            Toast.makeText(this, getString(R.string.error_field_required), Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_invalid_password)
            Toast.makeText(this, getString(R.string.error_invalid_password), Toast.LENGTH_SHORT).show()
            return false
        }
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = getString(R.string.error_field_required)
            Toast.makeText(this, getString(R.string.error_field_required), Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.error_passwords_dont_match)
            Toast.makeText(this, getString(R.string.error_passwords_dont_match), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun createAccount(fullName: String, email: String, password: String) {
        showLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Update user profile with full name
                    val profileUpdates = userProfileChangeRequest {
                        displayName = fullName
                    }
                    auth.currentUser?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            showLoading(false)
                            if (profileTask.isSuccessful) {
                                Toast.makeText(this, getString(R.string.success_signup), Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finishAffinity()
                            } else {
                                Toast.makeText(this, getString(R.string.error_auth_failed), Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    showLoading(false)
                    Toast.makeText(this, getString(R.string.error_auth_failed), Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSignUp.isEnabled = !show
        binding.etFullName.isEnabled = !show
        binding.etEmail.isEnabled = !show
        binding.etPassword.isEnabled = !show
        binding.etConfirmPassword.isEnabled = !show
        binding.tvLogin.isEnabled = !show
    }
} 