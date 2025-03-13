package com.example.ecotrack

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ecotrack.databinding.ActivityHouseholdBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class HouseholdActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHouseholdBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHouseholdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupSpinner(binding.spinnerAppliance, R.array.appliance_options)
        setupSpinner(binding.spinnerHeating, R.array.heating_options)
        setupSpinner(binding.spinnerAc, R.array.ac_options)
        setupSpinner(binding.spinnerWaterHeating, R.array.water_heating_options)
        setupSpinner(binding.spinnerLighting, R.array.lighting_options)

        binding.spinnerAppliance.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                updateSeekBarLabel(parent.getItemAtPosition(position).toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.seekbarUsage.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val appliance = binding.spinnerAppliance.selectedItem.toString()
                binding.tvUsageLabel.text = when (appliance) {
                    "Clothes Washing and Drying" -> "Number of wash cycles: ${progress + 1}"
                    "Dishwasher" -> "Number of wash cycles: ${progress + 1}"
                    "Used less hot water" -> "Reduction in hot water usage (liters): ${progress + 1}"
                    "Did not use hot water" -> "Total hot water saved (liters): ${progress + 1}"
                    "Solar Water Heater" -> "Usage hours per day: ${progress + 1}"
                    else -> "Duration not used (hours): ${progress + 1}"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnCalculateHousehold.setOnClickListener {
            val appliance = binding.spinnerAppliance.selectedItem.toString()
            val usage = binding.seekbarUsage.progress + 1

            try {
                val carbonFootprint = calculateCarbonFootprint(appliance, usage)
                saveCarbonFootprint(carbonFootprint)

                // Get suggestion and quote
                val suggestion = getSuggestion(carbonFootprint)
                val quote = getEcoFriendlyQuote()

                // Display everything in the same TextView
                binding.tvHouseholdCarbonSavings.text =
                    "🌍 Carbon Footprint: $carbonFootprint kg CO2eq\n\n" +
                            "🌿  $suggestion\n\n" +
                            "💡 \"$quote\""

                Toast.makeText(this, "Carbon footprint saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error calculating carbon footprint: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Clear the TextView when the activity is created
        binding.tvHouseholdCarbonSavings.text = ""
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

    private fun updateSeekBarLabel(appliance: String) {
        binding.tvUsageLabel.text = when (appliance) {
            "Clothes Washing and Drying" -> "Number of wash cycles per week"
            "Dishwasher" -> "Number of wash cycles per week"
            "Used less hot water" -> "Reduction in hot water usage (liters)"
            "Did not use hot water" -> "Total hot water saved (liters)"
            "Solar Water Heater" -> "Usage hours per day"
            else -> "Duration not used (hours)"
        }
    }

    private fun calculateCarbonFootprint(appliance: String, usage: Int): Double {
        return when (appliance) {
            "Refrigerator" -> usage * 0.1
            "Clothes Washing and Drying" -> usage * 0.5
            "Dishwasher" -> usage * 0.3
            "Cooking: Oven" -> usage * 0.2
            "Cooking: Stovetop/Cooktop/Hob" -> usage * 0.15
            "TV" -> usage * 0.05
            "Fan" -> usage * 0.02
            "Switched off heaters" -> usage * 0.4
            "Reduced temperature setting" -> usage * 0.3
            "Switched off AC" -> usage * 0.5
            "Increased temperature setting" -> usage * 0.4
            "Used less hot water" -> usage * 0.6
            "Did not use hot water" -> usage * 0.5
            "Solar Water Heater" -> usage * 0.2
            else -> 0.0
        }
    }

    private fun getSuggestion(carbonFootprint: Double): String {
        return when {
            carbonFootprint < 1 -> "Great job! Keep maintaining your eco-friendly habits! 🌍"
            carbonFootprint in 1.0..5.0 -> "You're doing well! Consider using LED bulbs and turning off unused devices. ⚡"
            carbonFootprint in 5.1..10.0 -> "Try reducing appliance usage time and switching to renewable energy sources. 🌱"
            else -> "Your carbon footprint is quite high. Consider making sustainable choices like solar power and energy-efficient appliances. 🔆"
        }
    }

    private fun getEcoFriendlyQuote(): String {
        val quotes = listOf(
            "The greatest threat to our planet is the belief that someone else will save it. – Robert Swan",
            "Small acts, when multiplied by millions of people, can transform the world. – Howard Zinn",
            "We do not inherit the Earth from our ancestors, we borrow it from our children. – Native American Proverb",
            "Reduce what you can, offset what you can’t. – Anonymous",
            "What we do now echoes in eternity. – Marcus Aurelius"
        )
        return quotes[Random.nextInt(quotes.size)]
    }

    private fun saveCarbonFootprint(carbonFootprint: Double) {
        val user = auth.currentUser
        if (user != null) {
            val timestamp = System.currentTimeMillis()
            val data = hashMapOf(
                "userId" to user.uid,
                "carbonFootprint" to carbonFootprint,
                "activityType" to "Household",
                "timestamp" to timestamp
            )
            db.collection("carbonFootprints").add(data)

            val sharedPreferences = getSharedPreferences("EcoTrackPrefs", Context.MODE_PRIVATE)
            val carbonFootprintList = sharedPreferences.getStringSet("householdCarbonFootprintList", mutableSetOf())?.toMutableSet()
            carbonFootprintList?.add("$carbonFootprint,$timestamp")
            with(sharedPreferences.edit()) {
                putStringSet("householdCarbonFootprintList", carbonFootprintList)
                apply()
            }
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getHouseholdCarbonFootprintList(): List<CarbonFootprintEntry> {
        val sharedPreferences = getSharedPreferences("EcoTrackPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet("householdCarbonFootprintList", setOf())?.map {
            val parts = it.split(",")
            CarbonFootprintEntry(parts[0].toDouble(), parts[1].toLong())
        } ?: listOf()
    }
}