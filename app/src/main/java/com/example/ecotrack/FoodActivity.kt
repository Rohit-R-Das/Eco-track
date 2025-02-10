package com.example.ecotrack

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FoodActivity : AppCompatActivity() {

    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerSubcategory: Spinner
    private lateinit var spinnerAdditionalOptions: Spinner
    private lateinit var tvAdditionalOptionsLabel: TextView
    private lateinit var tvFoodCarbonSavings: TextView
    private lateinit var btnCalculateFood: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth  // ✅ Initialize FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food)

        // ✅ Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        spinnerCategory = findViewById(R.id.spinner_category)
        spinnerSubcategory = findViewById(R.id.spinner_subcategory)
        spinnerAdditionalOptions = findViewById(R.id.spinner_additional_options)
        tvAdditionalOptionsLabel = findViewById(R.id.tv_additional_options_label)
        tvFoodCarbonSavings = findViewById(R.id.tvFoodCarbonSavings)
        btnCalculateFood = findViewById(R.id.btn_calculate_food)

        setupCategorySpinner()
        setupCalculateButton()
    }

    private fun setupCategorySpinner() {
        val categoryAdapter = ArrayAdapter.createFromResource(this,
            R.array.category_options, android.R.layout.simple_spinner_item)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                updateSubcategorySpinner(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateSubcategorySpinner(categoryPosition: Int) {
        val subcategoryArrayId = when (categoryPosition) {
            0 -> R.array.transport_options
            1 -> R.array.food_options
            2 -> R.array.appliance_category_options
            3 -> R.array.ict_options
            4 -> R.array.clothing_footwear_options
            5 -> R.array.housing_furniture_options
            6 -> R.array.waste_disposal_options
            else -> R.array.empty_options
        }

        val subcategoryAdapter = ArrayAdapter.createFromResource(this,
            subcategoryArrayId, android.R.layout.simple_spinner_item)
        subcategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSubcategory.adapter = subcategoryAdapter

        spinnerSubcategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                updateAdditionalOptionsSpinner(categoryPosition, position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateAdditionalOptionsSpinner(categoryPosition: Int, subcategoryPosition: Int) {
        val additionalOptionsArrayId = when {
            categoryPosition == 2 && (subcategoryPosition == 0 || subcategoryPosition == 1) -> R.array.appliance_additional_options
            categoryPosition == 3 && (subcategoryPosition == 0 || subcategoryPosition == 1 || subcategoryPosition == 2) -> R.array.ict_additional_options
            categoryPosition == 4 && (subcategoryPosition == 0 || subcategoryPosition == 1) -> R.array.clothing_footwear_additional_options
            categoryPosition == 5 && (subcategoryPosition == 0 || subcategoryPosition == 1) -> R.array.housing_furniture_additional_options
            else -> R.array.empty_options
        }

        if (additionalOptionsArrayId != R.array.empty_options) {
            tvAdditionalOptionsLabel.visibility = View.VISIBLE
            spinnerAdditionalOptions.visibility = View.VISIBLE

            val additionalOptionsAdapter = ArrayAdapter.createFromResource(this,
                additionalOptionsArrayId, android.R.layout.simple_spinner_item)
            additionalOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerAdditionalOptions.adapter = additionalOptionsAdapter
        } else {
            tvAdditionalOptionsLabel.visibility = View.GONE
            spinnerAdditionalOptions.visibility = View.GONE
        }
    }

    private fun setupCalculateButton() {
        btnCalculateFood.setOnClickListener {
            calculateCarbonFootprint()
        }
    }

    private fun calculateCarbonFootprint() {
        val category = spinnerCategory.selectedItem.toString()
        val subcategory = spinnerSubcategory.selectedItem.toString()
        val additionalOption = if (spinnerAdditionalOptions.visibility == View.VISIBLE) {
            spinnerAdditionalOptions.selectedItem.toString()
        } else {
            ""
        }

        // Implement the logic to calculate the carbon footprint based on the selected options
        val carbonFootprint = calculateFootprint(category, subcategory, additionalOption)

        // Update tvFoodCarbonSavings with the calculated value
        tvFoodCarbonSavings.text = "Carbon Savings: $carbonFootprint kg CO2"

        // Store the result in Firebase
        saveCarbonFootprint(category, subcategory, additionalOption, carbonFootprint)
    }

    private fun calculateFootprint(category: String, subcategory: String, additionalOption: String): Double {
        // Placeholder logic for carbon footprint calculation
        return when (category) {
            "Transport" -> 100.0
            "Food" -> 50.0
            "Appliances" -> 30.0
            "Information and Computer Technology" -> 20.0
            "Clothing and Footwear" -> 10.0
            "Housing and Furniture" -> 40.0
            "Waste Disposal" -> 5.0
            else -> 0.0
        }
    }

    private fun saveCarbonFootprint(category: String, subcategory: String, additionalOption: String, carbonFootprint: Double) {
        val user = auth.currentUser  // ✅ Use auth initialized in this activity
        if (user != null) {
            val data = hashMapOf(
                "userId" to user.uid,
                "category" to category,
                "subcategory" to subcategory,
                "additionalOption" to additionalOption,
                "carbonFootprint" to carbonFootprint,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("foodActivity")
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
