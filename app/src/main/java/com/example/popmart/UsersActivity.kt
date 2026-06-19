package com.example.popmart

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText

class UsersActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager
    private lateinit var adapter: UserAdapter
    private val users = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_users)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        userManager = UserManager(this)
        setupToolbar()
        setupRecyclerView()
        loadUsers()

        findViewById<FloatingActionButton>(R.id.fabAddUser).setOnClickListener {
            showAddEditUserDialog()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarUsers)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        val rvUsers = findViewById<RecyclerView>(R.id.rvUsers)
        adapter = UserAdapter(
            onEdit = { user -> showAddEditUserDialog(user) },
            onDelete = { user -> confirmDeleteUser(user) }
        )
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = adapter
    }

    private fun loadUsers() {
        users.clear()
        users.addAll(userManager.loadUsers())
        adapter.submitUsers(users)
    }

    private fun showAddEditUserDialog(existingUser: User? = null) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_user, null)
        val etUsername = view.findViewById<TextInputEditText>(R.id.etUsername)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etRole = view.findViewById<MaterialAutoCompleteTextView>(R.id.etRole)

        val roles = resources.getStringArray(R.array.role_options)
        etRole.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, roles))

        existingUser?.let {
            etUsername.setText(it.username)
            etEmail.setText(it.email)
            etRole.setText(it.role, false)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(if (existingUser == null) R.string.add_user else R.string.edit_user)
            .setView(view)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                val username = etUsername.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val role = etRole.text.toString().trim()

                if (username.isNotEmpty() && email.isNotEmpty() && role.isNotEmpty()) {
                    if (existingUser == null) {
                        val newUser = User(userManager.nextId(users), username, email, role)
                        users.add(newUser)
                    } else {
                        val index = users.indexOfFirst { it.id == existingUser.id }
                        if (index != -1) {
                            users[index] = existingUser.copy(username = username, email = email, role = role)
                        }
                    }
                    userManager.saveUsers(users)
                    adapter.submitUsers(users)
                } else {
                    Toast.makeText(this, R.string.required_fields_message, Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun confirmDeleteUser(user: User) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_user_title)
            .setMessage(getString(R.string.delete_user_message, user.username))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                users.remove(user)
                userManager.saveUsers(users)
                adapter.submitUsers(users)
            }
            .show()
    }
}
