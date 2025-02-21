package com.example.ecotrack

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
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

        val listView = findViewById<ListView>(R.id.listViewEntries)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val entries = combinedList.map { entry ->
            val date = Date(entry.timestamp)
            val formattedDate = format.format(date)
            "Carbon Footprint: ${entry.carbonFootprint} kg CO2eq\nUploaded on: $formattedDate"
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, entries)
        listView.adapter = adapter

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