package com.example.popmart

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var repository: CollectionRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: PopmartAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var tvSummaryUnique: TextView
    private lateinit var tvSummaryQuantity: TextView
    private lateinit var tvSummaryValue: TextView
    private lateinit var tvPageIndicator: TextView
    private lateinit var layoutPagination: View
    private lateinit var btnViewReport: MaterialButton
    private lateinit var btnManageUsers: MaterialButton
    private lateinit var btnPrevPage: MaterialButton
    private lateinit var btnNextPage: MaterialButton
    private lateinit var btnLogout: MaterialButton
    private lateinit var fabAdd: FloatingActionButton

    private val items = mutableListOf<PopmartItem>()
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private var currentPage = 0
    private val pageSize = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        repository = CollectionRepository(this)
        bindViews()
        setupRoleAccess()
        setupRecycler()

        items.clear()
        items.addAll(repository.loadItems())
        
        currentPage = 0
        refreshUi()

        fabAdd.setOnClickListener {
            showAddEditDialog()
        }
        btnViewReport.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }
        btnManageUsers.setOnClickListener {
            startActivity(Intent(this, UsersActivity::class.java))
        }
        btnPrevPage.setOnClickListener {
            if (currentPage > 0) {
                currentPage -= 1
                refreshUi()
            }
        }
        btnNextPage.setOnClickListener {
            val totalPages = calculateTotalPages()
            if (currentPage < totalPages - 1) {
                currentPage += 1
                refreshUi()
            }
        }
        btnLogout.setOnClickListener {
            sessionManager.logout()
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
        }
    }

    private fun bindViews() {
        recyclerView = findViewById(R.id.recyclerView)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        tvSummaryUnique = findViewById(R.id.tvSummaryUnique)
        tvSummaryQuantity = findViewById(R.id.tvSummaryQuantity)
        tvSummaryValue = findViewById(R.id.tvSummaryValue)
        tvPageIndicator = findViewById(R.id.tvPageIndicator)
        layoutPagination = findViewById(R.id.layoutPagination)
        btnViewReport = findViewById(R.id.btnViewReport)
        btnManageUsers = findViewById(R.id.btnManageUsers)
        btnPrevPage = findViewById(R.id.btnPrevPage)
        btnNextPage = findViewById(R.id.btnNextPage)
        btnLogout = findViewById(R.id.btnLogout)
        fabAdd = findViewById(R.id.fabAdd)
    }

    private fun setupRoleAccess() {
        val isSuperAdmin = sessionManager.isSuperAdmin()
        val isManager = sessionManager.isManager()
        val isAdmin = sessionManager.isAdmin()

        // Only SuperAdmin can manage users
        btnManageUsers.visibility = if (isSuperAdmin) View.VISIBLE else View.GONE
        
        // Only Manager or SuperAdmin can see reports
        btnViewReport.visibility = if (isManager) View.VISIBLE else View.GONE
        
        // Only Admins or SuperAdmins can add collections
        fabAdd.visibility = if (isAdmin) View.VISIBLE else View.GONE
    }

    private fun setupRecycler() {
        adapter = PopmartAdapter(
            onEdit = { item -> showAddEditDialog(item) },
            onDelete = { item -> confirmDelete(item) },
            canEdit = sessionManager.isAdmin()
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun showAddEditDialog(existing: PopmartItem? = null) {
        val form = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_item, null)

        val etName = form.findViewById<TextInputEditText>(R.id.etName)
        val etSeries = form.findViewById<TextInputEditText>(R.id.etSeries)
        val etRarity = form.findViewById<MaterialAutoCompleteTextView>(R.id.etRarity)
        val etDateAcquired = form.findViewById<TextInputEditText>(R.id.etDateAcquired)
        val etQuantity = form.findViewById<TextInputEditText>(R.id.etQuantity)
        val etUnitPrice = form.findViewById<TextInputEditText>(R.id.etUnitPrice)
        val etNotes = form.findViewById<TextInputEditText>(R.id.etNotes)

        setupDatePicker(etDateAcquired)

        val rarityOptions = resources.getStringArray(R.array.rarity_options)
        etRarity.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, rarityOptions))

        existing?.let { item ->
            etName.setText(item.name)
            etSeries.setText(item.series)
            etRarity.setText(item.rarity, false)
            etDateAcquired.setText(item.dateAcquired)
            etQuantity.setText(item.quantity.toString())
            etUnitPrice.setText(item.unitPrice.toString())
            etNotes.setText(item.notes)
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(if (existing == null) R.string.add_item_title else R.string.edit_item_title)
            .setView(form)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = etName.text?.toString()?.trim().orEmpty()
                val series = etSeries.text?.toString()?.trim().orEmpty()
                val rarity = etRarity.text?.toString()?.trim().orEmpty()
                val date = etDateAcquired.text?.toString()?.trim().orEmpty()
                val quantity = etQuantity.text?.toString()?.trim()?.toIntOrNull()
                val unitPrice = etUnitPrice.text?.toString()?.trim()?.toDoubleOrNull()
                val notes = etNotes.text?.toString()?.trim().orEmpty()

                val isValid = name.isNotBlank() &&
                    series.isNotBlank() &&
                    rarity.isNotBlank() &&
                    date.isNotBlank() &&
                    (quantity ?: 0) > 0 &&
                    (unitPrice ?: -1.0) >= 0.0

                if (!isValid) {
                    Toast.makeText(this, getString(R.string.required_fields_message), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (existing == null) {
                    val newItem = PopmartItem(
                        id = repository.nextId(items),
                        name = name,
                        series = series,
                        rarity = rarity,
                        dateAcquired = date,
                        quantity = quantity!!,
                        unitPrice = unitPrice!!,
                        notes = notes
                    )
                    items.add(0, newItem)
                    currentPage = 0
                } else {
                    val index = items.indexOfFirst { it.id == existing.id }
                    if (index != -1) {
                        items[index] = existing.copy(
                            name = name,
                            series = series,
                            rarity = rarity,
                            dateAcquired = date,
                            quantity = quantity!!,
                            unitPrice = unitPrice!!,
                            notes = notes
                        )
                    }
                }

                repository.saveItems(items)
                refreshUi()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun setupDatePicker(dateInput: TextInputEditText) {
        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val raw = dateInput.text?.toString()?.trim().orEmpty()

            val parts = raw.split("-")
            if (parts.size == 3) {
                val year = parts[0].toIntOrNull()
                val month = parts[1].toIntOrNull()
                val day = parts[2].toIntOrNull()
                if (year != null && month != null && day != null && month in 1..12 && day in 1..31) {
                    calendar.set(year, month - 1, day)
                }
            }

            DatePickerDialog(
                this,
                { _, year, monthOfYear, dayOfMonth ->
                    val picked = String.format(
                        Locale.US,
                        "%04d-%02d-%02d",
                        year,
                        monthOfYear + 1,
                        dayOfMonth
                    )
                    dateInput.setText(picked)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun confirmDelete(item: PopmartItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_title)
            .setMessage(R.string.delete_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                items.removeAll { it.id == item.id }
                repository.saveItems(items)
                refreshUi()
            }
            .show()
    }

    private fun refreshUi() {
        val totalPages = calculateTotalPages()
        currentPage = currentPage.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
        val startIndex = currentPage * pageSize
        val pageItems = items.drop(startIndex).take(pageSize)

        adapter.submitItems(pageItems)
        tvEmptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        layoutPagination.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        tvPageIndicator.text = getString(R.string.page_indicator, currentPage + 1, totalPages)
        btnPrevPage.isEnabled = currentPage > 0
        btnNextPage.isEnabled = currentPage < totalPages - 1

        val uniqueItems = items.size
        val totalQuantity = items.sumOf { it.quantity }
        val totalValue = items.sumOf { it.quantity * it.unitPrice }

        tvSummaryUnique.text = getString(R.string.summary_unique, uniqueItems)
        tvSummaryQuantity.text = getString(R.string.summary_quantity, totalQuantity)
        tvSummaryValue.text = getString(R.string.summary_value, currencyFormatter.format(totalValue))
    }

    private fun calculateTotalPages(): Int {
        return ((items.size + pageSize - 1) / pageSize).coerceAtLeast(1)
    }
}
