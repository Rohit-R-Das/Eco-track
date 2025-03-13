package com.example.ecotrack

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Tips : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var carbonFootprintText: TextView
    private lateinit var impactText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips)

        sharedPreferences = getSharedPreferences("EcoTrackPrefs", MODE_PRIVATE)
        carbonFootprintText = findViewById(R.id.txtCarbonFootprint)
        impactText = findViewById(R.id.txtImpact)

        // Fetch Carbon Footprint from Shared Preferences
        val totalCarbonFootprint: Float = sharedPreferences.getFloat("totalCarbonFootprint", 0.0f)
        carbonFootprintText.text = "Your Carbon Footprint: ${totalCarbonFootprint} kg CO₂"

        // Set Impact Level
        impactText.text = "Impact: ${getImpactLevel(totalCarbonFootprint)}"
    }

    private fun getImpactLevel(carbonFootprint: Float): String {
        return if (carbonFootprint <= 100) {
            "🌿 Low Impact"
        } else if (carbonFootprint > 100 && carbonFootprint <= 300) {
            "🌤️ Medium Impact"
        } else {
            "🔥 High Impact"
        }
    }
}
