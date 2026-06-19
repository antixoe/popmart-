package com.example.popmart

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import java.io.OutputStreamWriter
import java.text.NumberFormat
import java.util.Locale

class ReportActivity : AppCompatActivity() {

    private lateinit var repository: CollectionRepository
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reportRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarReport)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_export -> {
                    exportToCsv()
                    true
                }
                else -> false
            }
        }

        repository = CollectionRepository(this)
        populateReport(repository.loadItems())
    }

    private fun populateReport(items: List<PopmartItem>) {
        val uniqueItems = items.size
        val totalQuantity = items.sumOf { it.quantity }
        val totalValue = items.sumOf { it.quantity * it.unitPrice }
        val averagePrice = if (items.isNotEmpty()) items.map { it.unitPrice }.average() else 0.0
        val seriesCount = items.map { it.series }.toSet().size

        val mostCommonRarity = items
            .groupingBy { it.rarity }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?: getString(R.string.report_no_data)

        val latestDate = items
            .maxByOrNull { it.dateAcquired }
            ?.dateAcquired
            ?: getString(R.string.report_no_data)

        findViewById<TextView>(R.id.tvReportUnique).text =
            getString(R.string.report_total_unique, uniqueItems)
        findViewById<TextView>(R.id.tvReportQuantity).text =
            getString(R.string.report_total_quantity, totalQuantity)
        findViewById<TextView>(R.id.tvReportValue).text =
            getString(R.string.report_total_value, currencyFormatter.format(totalValue))
        findViewById<TextView>(R.id.tvReportAverage).text =
            getString(R.string.report_avg_price, currencyFormatter.format(averagePrice))
        findViewById<TextView>(R.id.tvReportSeriesCount).text =
            getString(R.string.report_series_count, seriesCount)
        findViewById<TextView>(R.id.tvReportTopRarity).text =
            getString(R.string.report_top_rarity, mostCommonRarity)
        findViewById<TextView>(R.id.tvReportLatestDate).text =
            getString(R.string.report_latest_acquired, latestDate)

        val rarityBreakdown = items
            .groupingBy { it.rarity }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .joinToString(separator = "\n") {
                getString(R.string.report_breakdown_line, it.key, it.value)
            }
            .ifBlank { getString(R.string.report_empty_breakdown) }

        findViewById<TextView>(R.id.tvRarityBreakdown).text = rarityBreakdown
    }

    private fun exportToCsv() {
        val items = repository.loadItems()
        if (items.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        val csvHeader = "ID,Name,Series,Rarity,Date Acquired,Quantity,Unit Price,Notes\n"
        val csvData = items.joinToString("\n") { 
            "${it.id},\"${it.name}\",\"${it.series}\",\"${it.rarity}\",${it.dateAcquired},${it.quantity},${it.unitPrice},\"${it.notes}\""
        }
        val csvContent = csvHeader + csvData

        val fileName = "Popmart_Collection_Report_${System.currentTimeMillis()}.csv"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        OutputStreamWriter(outputStream).use { writer ->
                            writer.write(csvContent)
                        }
                    }
                    Toast.makeText(this, getString(R.string.export_success), Toast.LENGTH_LONG).show()
                } ?: throw Exception("Failed to create URI")
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(downloadsDir, fileName)
                file.writeText(csvContent)
                Toast.makeText(this, getString(R.string.export_success), Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "${getString(R.string.export_failed)}: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
