package com.example.managecontact.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.managecontact.R
import com.example.managecontact.data.model.Contact
import com.example.managecontact.databinding.ActivityMainBinding
import com.example.managecontact.ui.auth.LoginActivity
import com.example.managecontact.ui.contact.AddEditContactActivity
import com.example.managecontact.ui.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ContactAdapter
    private var contacts = mutableListOf<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        auth = Firebase.auth
        db = Firebase.firestore

        setupRecyclerView()
        setupClickListeners()
        setupSearch()
        loadContacts()
    }

    private fun setupRecyclerView() {
        adapter = ContactAdapter(
            onItemClick = { contact ->
                startActivity(
                    Intent(this, AddEditContactActivity::class.java)
                        .putExtra(AddEditContactActivity.EXTRA_CONTACT, contact)
                )
            },
            onItemSwipe = { contact ->
                showDeleteConfirmationDialog(contact)
            }
        )

        binding.rvContacts.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        // Setup swipe to delete/edit
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val contact = adapter.currentList[position]
                when (direction) {
                    ItemTouchHelper.LEFT -> showDeleteConfirmationDialog(contact)
                    ItemTouchHelper.RIGHT -> {
                        startActivity(
                            Intent(this@MainActivity, AddEditContactActivity::class.java)
                                .putExtra(AddEditContactActivity.EXTRA_CONTACT, contact)
                        )
                    }
                }
                adapter.notifyItemChanged(position)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvContacts)
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditContactActivity::class.java))
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            val query = text.toString().trim().lowercase()
            val filteredContacts = if (query.isEmpty()) {
                contacts
            } else {
                contacts.filter { contact ->
                    contact.name.lowercase().contains(query) ||
                            contact.phoneNumber.contains(query)
                }
            }
            adapter.submitList(filteredContacts)
        }
    }

    private fun loadContacts() {
        showLoading(true)
        db.collection("contacts")
            .whereEqualTo("userId", auth.currentUser?.uid)
            .addSnapshotListener { snapshot, error ->
                showLoading(false)
                if (error != null) {
                    Toast.makeText(this, getString(R.string.error_network), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                contacts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Contact::class.java)?.apply { id = doc.id }
                }?.sortedBy { it.name }?.toMutableList() ?: mutableListOf()

                adapter.submitList(contacts)
            }
    }

    private fun showDeleteConfirmationDialog(contact: Contact) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_contact)
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteContact(contact)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteContact(contact: Contact) {
        showLoading(true)
        db.collection("contacts").document(contact.id)
            .delete()
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, R.string.success_contact_deleted, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.action_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 