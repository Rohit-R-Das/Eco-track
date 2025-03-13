package com.example.ecotrack

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WasteActivity : AppCompatActivity() {

    private lateinit var wasteSpinner: Spinner
    private lateinit var subcategorySpinner: Spinner
    private lateinit var weightSeekBar: SeekBar
    private lateinit var weightTextView: TextView
    private lateinit var calculateButton: Button
    private lateinit var tvWasteCarbonSavings: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waste)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        wasteSpinner = findViewById(R.id.wasteSpinner)
        subcategorySpinner = findViewById(R.id.subcategorySpinner)
        weightSeekBar = findViewById(R.id.weightSeekBar)
        weightTextView = findViewById(R.id.weightTextView)
        calculateButton = findViewById(R.id.calculateButton)
        tvWasteCarbonSavings = findViewById(R.id.tvWasteCarbonSavings)

        // Set up Waste Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.waste_categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            wasteSpinner.adapter = adapter
        }

        // Handle Waste Spinner selection
        wasteSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedWaste = parent?.getItemAtPosition(position).toString()
                updateSubcategories(selectedWaste)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Handle SeekBar changes
        weightSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val weight = progress + 10 // Range: 10kg to 200kg
                weightTextView.text = "Weight: $weight kg"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Handle Calculate Button click
        calculateButton.setOnClickListener {
            val selectedWaste = wasteSpinner.selectedItem.toString()
            val selectedSubcategory = subcategorySpinner.selectedItem.toString()
            val weight = weightSeekBar.progress + 10

            // Calculate carbon footprint
            val carbonFootprint = calculateCarbonFootprint(selectedWaste, selectedSubcategory, weight)

            // Get suggestion and quote
            val suggestion = getSuggestion(carbonFootprint)

            // Display in TextView
            tvWasteCarbonSavings.text = "Carbon Savings: $carbonFootprint kg CO2\n\n$suggestion"

            // Save to Firebase and SharedPreferences
            saveCarbonFootprint(selectedWaste, selectedSubcategory, weight, carbonFootprint)
        }
    }

    private fun updateSubcategories(selectedWaste: String) {
        val subcategoryArrayId = when (selectedWaste) {
            "Wet Waste" -> R.array.wet_waste_subcategories
            "Dry Waste" -> R.array.dry_waste_subcategories
            "Electronic Waste" -> R.array.electronic_waste_subcategories
            "Clothes" -> R.array.clothes_subcategories
            "Footwear" -> R.array.footwear_subcategories
            "Furniture" -> R.array.furniture_subcategories
            else -> return
        }

        ArrayAdapter.createFromResource(
            this,
            subcategoryArrayId,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            subcategorySpinner.adapter = adapter
            subcategorySpinner.visibility = View.VISIBLE
        }
    }

    private fun calculateCarbonFootprint(waste: String, subcategory: String, weight: Int): Double {
        return weight * 0.1 // Example: 0.1 kg CO2 per kg of waste
    }

    private fun getSuggestion(carbonFootprint: Double): String {
        return when {
            carbonFootprint > 50 -> "You're making a great impact! Keep recycling and reducing waste.\n'Be the change you wish to see in the world.'"
            carbonFootprint > 30 -> "Good job! Composting food waste can reduce emissions further.\n'Small actions make a big difference.'"
            carbonFootprint > 10 -> "Nice effort! Consider reusing materials whenever possible.\n'The greatest threat to our planet is the belief that someone else will save it.'"
            else -> "Fantastic! You're making eco-friendly choices every day!\n'The Earth is what we all have in common.'"
        }
    }

    private fun saveCarbonFootprint(waste: String, subcategory: String, weight: Int, carbonFootprint: Double) {
        val user = auth.currentUser
        if (user != null) {
            val timestamp = System.currentTimeMillis()
            val data = hashMapOf(
                "userId" to user.uid,
                "activityType" to "Waste",
                "waste" to waste,
                "subcategory" to subcategory,
                "weight" to weight,
                "carbonFootprint" to carbonFootprint,
                "timestamp" to timestamp
            )
            db.collection("carbonFootprints")
                .add(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            // Save to SharedPreferences
            val sharedPreferences = getSharedPreferences("EcoTrackPrefs", Context.MODE_PRIVATE)
            val carbonFootprintList = sharedPreferences.getStringSet("wasteCarbonFootprintList", mutableSetOf())?.toMutableSet()
            val newEntry = "$carbonFootprint,$timestamp"
            carbonFootprintList?.add(newEntry)
            with(sharedPreferences.edit()) {
                putStringSet("wasteCarbonFootprintList", carbonFootprintList)
                apply()
            }
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
        }
    }

}
