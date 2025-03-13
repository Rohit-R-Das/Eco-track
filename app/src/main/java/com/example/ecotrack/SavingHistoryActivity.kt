package com.example.ecotrack

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SavingHistoryActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tvTotalCarbonFootprint: TextView
    private lateinit var btnClearHistory: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saving_history)

        sharedPreferences = getSharedPreferences("EcoTrackPrefs", Context.MODE_PRIVATE)
        tvTotalCarbonFootprint = findViewById(R.id.tvTotalCarbonFootprint)
        btnClearHistory = findViewById(R.id.btnClearHistory)

        val carbonFootprintList = getCarbonFootprintList("carbonFootprintList")
        val householdCarbonFootprintList = getCarbonFootprintList("householdCarbonFootprintList")
        val wasteCarbonFootprintList = getCarbonFootprintList("wasteCarbonFootprintList")
        val foodCarbonFootprintList = getCarbonFootprintList("foodCarbonFootprintList")

        val combinedList = carbonFootprintList + householdCarbonFootprintList + wasteCarbonFootprintList + foodCarbonFootprintList

        val tableLayout = findViewById<TableLayout>(R.id.tableLayoutEntries)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        for (entry in combinedList) {
            val date = Date(entry.timestamp)
            val formattedDate = format.format(date)

            // Create a new table row
            val tableRow = TableRow(this)

            // Carbon Footprint TextView
            val tvFootprint = TextView(this).apply {
                text = "${entry.carbonFootprint} kg CO2eq"
                setPadding(16, 16, 16, 16)
            }

            // Uploaded Date TextView
            val tvDate = TextView(this).apply {
                text = formattedDate
                setPadding(16, 16, 16, 16)
            }

            // Add TextViews to the TableRow
            tableRow.addView(tvFootprint)
            tableRow.addView(tvDate)

            // Add the TableRow to the TableLayout
            tableLayout.addView(tableRow)
        }

        // Calculate total carbon footprint
        val totalCarbonFootprint = combinedList.sumOf { it.carbonFootprint }
        tvTotalCarbonFootprint.text = "Total Carbon Footprint: $totalCarbonFootprint kg CO2eq"

        // Save total carbon footprint to SharedPreferences
        saveTotalCarbonFootprint(totalCarbonFootprint)

        // Clear history button click listener
        btnClearHistory.setOnClickListener {
            clearHistory()
        }
    }

    private fun getCarbonFootprintList(key: String): List<CarbonFootprintEntry> {
        return sharedPreferences.getStringSet(key, setOf())?.map {
            val parts = it.split(",")
            CarbonFootprintEntry(parts[0].toDouble(), parts[1].toLong())
        } ?: listOf()
    }

    private fun saveTotalCarbonFootprint(totalCarbonFootprint: Double) {
        with(sharedPreferences.edit()) {
            putFloat("totalCarbonFootprint", totalCarbonFootprint.toFloat())
            apply()
        }
    }


    private fun clearHistory() {
        with(sharedPreferences.edit()) {
            remove("carbonFootprintList")
            remove("householdCarbonFootprintList")
            remove("wasteCarbonFootprintList")
            remove("foodCarbonFootprintList")
            remove("totalCarbonFootprint")
            apply()
        }
        recreate() // Refresh the activity to update the UI
    }

}
