package com.example.ecotrack

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
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
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food)

        // Initialize Firebase
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
        val categoryList = listOf(
            "Appliances", "Information and Computer Technology",
            "Clothing and Footwear", "Housing and Furniture"
        )

        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
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
        val subcategoryArray = when (categoryPosition) {
            0 -> R.array.appliance_main_options  // Large / Small Appliance
            1 -> R.array.ict_main_options       // Mobile / Laptop / Other
            2 -> R.array.clothing_footwear_options
            3 -> R.array.housing_furniture_options
            else -> R.array.empty_options
        }

        val subcategoryAdapter = ArrayAdapter.createFromResource(
            this, subcategoryArray, android.R.layout.simple_spinner_item
        )
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
            categoryPosition == 0 -> R.array.appliance_additional_options
            categoryPosition == 1 -> R.array.ict_additional_options
            categoryPosition == 2 -> R.array.clothing_footwear_additional_options
            categoryPosition == 3 -> R.array.housing_furniture_additional_options
            else -> R.array.empty_options
        }

        if (additionalOptionsArrayId != R.array.empty_options) {
            tvAdditionalOptionsLabel.visibility = View.VISIBLE
            spinnerAdditionalOptions.visibility = View.VISIBLE

            val additionalOptionsAdapter = ArrayAdapter.createFromResource(
                this, additionalOptionsArrayId, android.R.layout.simple_spinner_item
            )
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

        // Calculate carbon footprint
        val carbonFootprint = calculateFootprint(category, subcategory, additionalOption)

        // Get praise message
        val praiseMessage = getPraiseMessage(carbonFootprint)

        // Display the result with praise
        tvFoodCarbonSavings.text = "Carbon Savings: $carbonFootprint kg CO2\n\n$praiseMessage"

        // Save data to Firebase and SharedPreferences
        saveCarbonFootprint(category, subcategory, additionalOption, carbonFootprint)
    }

    private fun calculateFootprint(category: String, subcategory: String, additionalOption: String): Double {
        return when (category) {
            "Appliances" -> 30.0
            "Information and Computer Technology" -> 20.0
            "Clothing and Footwear" -> 10.0
            "Housing and Furniture" -> 40.0
            else -> 0.0
        }
    }

    private fun getPraiseMessage(carbonFootprint: Double): String {
        return when {
            carbonFootprint > 80 -> "🌱 Amazing! You're making a big impact on reducing carbon emissions!"
            carbonFootprint > 50 -> "💚 Great job! Every step counts towards a greener planet!"
            carbonFootprint > 30 -> "🌍 Keep it up! Small changes lead to a big difference."
            else -> "👏 Fantastic! Sustainable choices make the world a better place."
        }
    }

    private fun saveCarbonFootprint(category: String, subcategory: String, additionalOption: String, carbonFootprint: Double) {
        val user = auth.currentUser
        if (user != null) {
            val timestamp = Timestamp.now()

            val data = hashMapOf(
                "userId" to user.uid,
                "activityType" to "Food",
                "category" to category,
                "subcategory" to subcategory,
                "additionalOption" to additionalOption,
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
            val carbonFootprintList = sharedPreferences.getStringSet("foodCarbonFootprintList", mutableSetOf())?.toMutableSet()
            val newEntry = "$carbonFootprint,${System.currentTimeMillis()}"
            carbonFootprintList?.add(newEntry)
            with(sharedPreferences.edit()) {
                putStringSet("foodCarbonFootprintList", carbonFootprintList)
                apply()
            }
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
        }
    }

}