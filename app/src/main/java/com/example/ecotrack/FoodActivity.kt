package com.example.ecotrack

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FoodActivity : AppCompatActivity() {

    private lateinit var etMeatConsumption: EditText
    private lateinit var etDairyConsumption: EditText
    private lateinit var etVegetableConsumption: EditText
    private lateinit var btnCalculateFood: Button
    private lateinit var tvFoodCarbonSavings: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food)

        etMeatConsumption = findViewById(R.id.et_meat_consumption)
        etDairyConsumption = findViewById(R.id.et_dairy_consumption)
        etVegetableConsumption = findViewById(R.id.et_vegetable_consumption)
        btnCalculateFood = findViewById(R.id.btn_calculate_food)
        tvFoodCarbonSavings = findViewById(R.id.tvFoodCarbonSavings)

        btnCalculateFood.setOnClickListener {
            calculateCarbonSavings()
        }
    }

    private fun calculateCarbonSavings() {
        val meatConsumption = etMeatConsumption.text.toString().toDoubleOrNull() ?: 0.0
        val dairyConsumption = etDairyConsumption.text.toString().toDoubleOrNull() ?: 0.0
        val vegetableConsumption = etVegetableConsumption.text.toString().toDoubleOrNull() ?: 0.0

        val carbonSavings = calculateCarbonFootprint(meatConsumption, dairyConsumption, vegetableConsumption)
        tvFoodCarbonSavings.text = "Carbon Savings: $carbonSavings kg CO2"
    }

    private fun calculateCarbonFootprint(meat: Double, dairy: Double, vegetables: Double): Double {
        val meatFactor = 27.0 // kg CO2 per kg of meat
        val dairyFactor = 13.0 // kg CO2 per kg of dairy
        val vegetableFactor = 2.0 // kg CO2 per kg of vegetables

        return (meat * meatFactor) + (dairy * dairyFactor) + (vegetables * vegetableFactor)
    }
}