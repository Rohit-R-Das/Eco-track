package com.example.ecotrack

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.ecotrack.databinding.ActivityCommuteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommuteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommuteBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommuteBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Setup Spinners
        setupSpinner(binding.spinnerTravelBy, R.array.travel_modes)
        setupSpinner(binding.spinnerInsteadOf, R.array.instead_of_options)

        // Calculate Button Click
        binding.btnCalculate.setOnClickListener {
            calculateAndSaveCarbonFootprint()
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

    private fun calculateAndSaveCarbonFootprint() {
        val distanceStr = binding.etDistance.text.toString()
        val distance = distanceStr.toDoubleOrNull()
        val travelMode = binding.spinnerTravelBy.selectedItem?.toString()
        val insteadOf = binding.spinnerInsteadOf.selectedItem?.toString()

        if (distance == null || travelMode.isNullOrEmpty() || insteadOf.isNullOrEmpty()) {
            Toast.makeText(this, "Please enter valid distance and select options.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val carbonFootprint = calculateCarbonFootprint(distance, travelMode, insteadOf)
            binding.tvCarbonSavings.text = "$carbonFootprint kg CO2eq"
            saveCarbonFootprint(carbonFootprint)
        } catch (e: Exception) {
            Toast.makeText(this, "Error calculating carbon footprint: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateCarbonFootprint(distance: Double, travelMode: String, insteadOf: String): Double {
        val emissionFactor = getEmissionFactor(travelMode)
        val insteadOfFactor = getEmissionFactor(insteadOf)
        return distance * (insteadOfFactor - emissionFactor)
    }

    private fun getEmissionFactor(mode: String): Double {
        return when (mode) {
            "Electrical Bicycle" -> 0.02
            "Bus/Minibus/Coach" -> 0.05
            "Train/Metro" -> 0.04
            "2 Wheeler (alone)" -> 0.06
            "2 Wheeler (2 passengers)" -> 0.03
            "Car (alone)" -> 0.12
            "Car (2 passengers)" -> 0.06
            "Car (3 passengers)" -> 0.04
            "Car (4 passengers)" -> 0.03
            "Flight" -> 0.15
            "Bicycle" -> 0.0
            "Did not travel" -> 0.0
            "Feet! (walking)" -> 0.0
            else -> 0.0
        }
    }

    private fun saveCarbonFootprint(carbonFootprint: Double) {
        val user = auth.currentUser
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