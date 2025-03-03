package com.example.managecontact.ui.contact

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.managecontact.R
import com.example.managecontact.data.model.Contact
import com.example.managecontact.databinding.ActivityAddEditContactBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddEditContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditContactBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var contact: Contact? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        contact = intent.getParcelableExtra(EXTRA_CONTACT)
        setupToolbar()
        setupClickListeners()
        populateData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = if (contact == null) getString(R.string.add_contact) else getString(R.string.edit_contact)
        }
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            if (validateInput(name, phone)) {
                saveContact(name, phone)
            }
        }
    }

    private fun populateData() {
        contact?.let {
            binding.etName.setText(it.name)
            binding.etPhone.setText(it.phoneNumber)
        }
    }

    private fun validateInput(name: String, phone: String): Boolean {
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.error_field_required)
            return false
        }
        if (phone.isEmpty()) {
            binding.tilPhone.error = getString(R.string.error_field_required)
            return false
        }
        if (!android.util.Patterns.PHONE.matcher(phone).matches()) {
            binding.tilPhone.error = getString(R.string.error_invalid_phone)
            return false
        }
        return true
    }

    private fun saveContact(name: String, phone: String) {
        showLoading(true)

        val contactData = mapOf(
            "name" to name,
            "phoneNumber" to phone,
            "userId" to auth.currentUser?.uid,
            "createdAt" to System.currentTimeMillis()
        )

        if (contact == null) {
            // Add new contact
            db.collection("contacts")
                .add(contactData)
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(this, R.string.success_contact_added, Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    showLoading(false)
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        } else {
            // Update existing contact
            db.collection("contacts")
                .document(contact!!.id)
                .update(contactData as Map<String, Any>)
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(this, R.string.success_contact_updated, Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    showLoading(false)
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !show
        binding.etName.isEnabled = !show
        binding.etPhone.isEnabled = !show
    }

    companion object {
        const val EXTRA_CONTACT = "extra_contact"
    }
} 