package com.example.ecotrack

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ecotrack.databinding.ActivityHouseholdBinding
import com.google.firebase.firestore.FirebaseFirestore

class HouseholdActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHouseholdBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHouseholdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Set up the spinners with appliance options
        setupSpinner(binding.spinnerAppliance, R.array.appliance_options)
        setupSpinner(binding.spinnerHeating, R.array.heating_options)
        setupSpinner(binding.spinnerAc, R.array.ac_options)
        setupSpinner(binding.spinnerWaterHeating, R.array.water_heating_options)
        setupSpinner(binding.spinnerLighting, R.array.lighting_options)

        // Set up the spinner selection listener
        binding.spinnerAppliance.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                updateSeekBarLabel(parent.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Set up the SeekBar change listener
        binding.seekbarUsage.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvUsageLabel.text = "Usage: ${progress + 1}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.seekbarLightingDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvLightingDurationLabel.text = "Lighting Duration: ${progress + 1} hours"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnCalculateHousehold.setOnClickListener {
            val appliance = binding.spinnerAppliance.selectedItem.toString()
            val usage = binding.seekbarUsage.progress + 1
            val lightingDuration = binding.seekbarLightingDuration.progress + 1

            try {
                val carbonFootprint = calculateCarbonFootprint(appliance, usage, lightingDuration)
                saveCarbonFootprint(carbonFootprint)
                binding.tvHouseholdCarbonSavings.text = "$carbonFootprint kg CO2eq"
                Toast.makeText(this, "Carbon footprint saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error calculating carbon footprint: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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

    private fun updateSeekBarLabel(appliance: String) {
        binding.tvUsageLabel.text = when (appliance) {
            "Refrigerator" -> "Duration not used (hours)"
            "Clothes Washing and Drying" -> "Number of wash cycles"
            "Dishwasher" -> "Number of wash cycles"
            "Cooking: Oven" -> "Duration not used (hours)"
            "Cooking: Stovetop/Cooktop/Hob" -> "Duration not used (hours)"
            "TV" -> "Duration not used (hours)"
            "Fan" -> "Duration not used (hours)"
            "Switched of heaters" -> "Duration not used (hours)"
            "Reduced temperature Setting" -> "Duration not used (hours)"
            "Switched Off AC" -> "Duration not used (hours)"
            "Increased Temperature Setting" -> "Duration not used (hours)"
            "Used Less Hot water" -> "Quantity of water (liters)"
            "Did'n used Hot Water" -> "Quantity of water (liters)"
            "Switched of light bulbs" -> "Duration not used (hours)"
            else -> "Usage"
        }
    }

    private fun calculateCarbonFootprint(appliance: String, usage: Int, lightingDuration: Int): Double {
        val applianceFootprint = when (appliance) {
            "Refrigerator" -> usage * 0.1 // Example factor
            "Clothes Washing and Drying" -> usage * 0.5 // Example factor
            "Dishwasher" -> usage * 0.3 // Example factor
            "Cooking: Oven" -> usage * 0.2 // Example factor
            "Cooking: Stovetop/Cooktop/Hob" -> usage * 0.15 // Example factor
            "TV" -> usage * 0.05 // Example factor
            "Fan" -> usage * 0.02 // Example factor
            "Switched of heaters" -> usage * 0.4 // Example factor
            "Reduced temperature Setting" -> usage * 0.3 // Example factor
            "Switched Off AC" -> usage * 0.5 // Example factor
            "Increased Temperature Setting" -> usage * 0.4 // Example factor
            "Used Less Hot water" -> usage * 0.6 // Example factor
            "Did'n used Hot Water" -> usage * 0.5 // Example factor
            "Switched of light bulbs" -> 0.0 // No usage for switched off bulbs
            else -> 0.0
        }
        val lightingFootprint = lightingDuration * 0.1 // Example factor for lighting
        return applianceFootprint + lightingFootprint
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