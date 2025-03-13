package com.example.ecotrack

import android.content.Context
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
            val formattedCarbon = "%.2f".format(carbonFootprint)

            // Generate Feedback & Quote
            val (message, quote) = generateFeedbackAndQuote(carbonFootprint)

            binding.tvCarbonSavings.text = """
                $message
                
                🌱 "$quote"
                
                🌍 Carbon Footprint: $formattedCarbon kg CO2eq
            """.trimIndent()

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

    private fun generateFeedbackAndQuote(carbonFootprint: Double): Pair<String, String> {
        val quotes = listOf(
            "Every small step towards sustainability counts!",
            "Be the change. Reduce your carbon footprint.",
            "A cleaner future starts with your choices today.",
            "Nature doesn't need us, but we need nature.",
            "Drive less, breathe more!"
        )
        val randomQuote = quotes.random()

        val message = when {
            carbonFootprint < -1.0 -> "Oops! Your choice increased emissions. Consider greener options like public transport or biking. 🌍"
            carbonFootprint in -1.0..1.0 -> "You're making a balanced choice! Try walking or carpooling when possible. 🚶‍♂️🚗"
            carbonFootprint > 1.0 -> "Fantastic! You saved ${"%.2f".format(carbonFootprint)} kg of CO2! Keep up the great eco-friendly habits. 🌿"
            else -> "Your choice has a neutral impact. Small steps make a big difference over time!"
        }

        return Pair(message, randomQuote)
    }

    private fun saveCarbonFootprint(carbonFootprint: Double) {
        val user = auth.currentUser
        if (user != null) {
            val timestamp = System.currentTimeMillis()
            val data = hashMapOf(
                "userId" to user.uid,
                "carbonFootprint" to carbonFootprint,
                "activityType" to "Commute",
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
            val carbonFootprintList = sharedPreferences.getStringSet("carbonFootprintList", mutableSetOf())?.toMutableSet()
            val newEntry = "$carbonFootprint,$timestamp"
            carbonFootprintList?.add(newEntry)
            with(sharedPreferences.edit()) {
                putStringSet("carbonFootprintList", carbonFootprintList)
                apply()
            }
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
        }
    }
}
