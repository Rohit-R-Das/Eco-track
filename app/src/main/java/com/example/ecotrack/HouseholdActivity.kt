package com.example.ecotrack

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HouseholdActivity : AppCompatActivity() {

    private lateinit var etElectricityUsage: EditText
    private lateinit var etWaterUsage: EditText
    private lateinit var etGasUsage: EditText
    private lateinit var etWasteManagement: EditText
    private lateinit var btnCalculateHousehold: Button
    private lateinit var tvHouseholdCarbonSavings: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_household)

        etElectricityUsage = findViewById(R.id.et_electricity_usage)
        etWaterUsage = findViewById(R.id.et_water_usage)
        etGasUsage = findViewById(R.id.et_gas_usage)
        etWasteManagement = findViewById(R.id.et_waste_management)
        btnCalculateHousehold = findViewById(R.id.btn_calculate_household)
        tvHouseholdCarbonSavings = findViewById(R.id.tvHouseholdCarbonSavings)

        btnCalculateHousehold.setOnClickListener {
            calculateCarbonSavings()
        }
    }

    private fun calculateCarbonSavings() {
        val electricityUsage = etElectricityUsage.text.toString().toDoubleOrNull() ?: 0.0
        val waterUsage = etWaterUsage.text.toString().toDoubleOrNull() ?: 0.0
        val gasUsage = etGasUsage.text.toString().toDoubleOrNull() ?: 0.0
        val wasteManagement = etWasteManagement.text.toString().toDoubleOrNull() ?: 0.0

        val carbonSavings = calculateCarbonFootprint(electricityUsage, waterUsage, gasUsage, wasteManagement)
        tvHouseholdCarbonSavings.text = "Carbon Savings: $carbonSavings kg CO2"
    }

    private fun calculateCarbonFootprint(electricity: Double, water: Double, gas: Double, waste: Double): Double {
        val electricityFactor = 0.5 // kg CO2 per kWh
        val waterFactor = 0.3 // kg CO2 per liter
        val gasFactor = 2.0 // kg CO2 per cubic meter
        val wasteFactor = 1.5 // kg CO2 per kg

        return (electricity * electricityFactor) + (water * waterFactor) + (gas * gasFactor) + (waste * wasteFactor)
    }
}