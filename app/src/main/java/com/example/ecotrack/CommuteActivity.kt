package com.example.ecotrack

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ecotrack.databinding.ActivityCommuteBinding
import com.google.firebase.firestore.FirebaseFirestore

class CommuteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommuteBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommuteBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Set up the spinners with travel modes and instead of options
        setupSpinner(binding.spinnerTravelBy, R.array.travel_modes)
        setupSpinner(binding.spinnerInsteadOf, R.array.instead_of_options)

        binding.btnCalculate.setOnClickListener {
            val distance = binding.etDistance.text.toString().toDoubleOrNull()
            val travelMode = binding.spinnerTravelBy.selectedItem.toString()
            val insteadOf = binding.spinnerInsteadOf.selectedItem.toString()

            if (distance != null) {
                try {
                    val carbonFootprint = calculateCarbonFootprint(distance, travelMode, insteadOf)
                    saveCarbonFootprint(carbonFootprint)
                    binding.tvCarbonSavings.text = "$carbonFootprint kg CO2eq"
                    Toast.makeText(this, "Carbon footprint saved!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error calculating carbon footprint: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a valid distance.", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupSpinner(spinner: Spinner, arrayResId: Int) {
        val adapter = ArrayAdapter.createFromResource(
            this,
            arrayResId,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun calculateCarbonFootprint(distance: Double, travelMode: String, insteadOf: String): Double {
        val emissionFactor = when (travelMode) {
            "Car" -> 0.21 // kg CO2 per km
            "Bus" -> 0.05
            "Train" -> 0.04
            "Bicycle" -> 0.0
            "Walking" -> 0.0
            else -> 0.0
        }
        val insteadOfFactor = when (insteadOf) {
            "Car" -> 0.21
            "Bus" -> 0.05
            "Train" -> 0.04
            "Bicycle" -> 0.0
            "Walking" -> 0.0
            else -> 0.0
        }
        return distance * (insteadOfFactor - emissionFactor)
    }

    private fun saveCarbonFootprint(carbonFootprint: Double) {
        val user = MainActivity.auth.currentUser
        if (user != null) {
            val data = hashMapOf(
                "userId" to user.uid,
                "carbonFootprint" to carbonFootprint,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("carbonFootprints")
                .add(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
        }
    }
}