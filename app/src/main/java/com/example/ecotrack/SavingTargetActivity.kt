package com.example.ecotrack

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SavingTargetActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var etGoal: EditText
    private lateinit var btnSaveGoal: Button
    private lateinit var btnResetGoal: Button
    private lateinit var tvGoalDisplay: TextView
    private lateinit var tvGoalLeft: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saving_target)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("EcoTrackPrefs", Context.MODE_PRIVATE)

        etGoal = findViewById(R.id.etGoal)
        btnSaveGoal = findViewById(R.id.btnSaveGoal)
        btnResetGoal = findViewById(R.id.btnResetGoal)
        tvGoalDisplay = findViewById(R.id.tvGoalDisplay)
        tvGoalLeft = findViewById(R.id.tvGoalLeft)

        loadSavedGoal()

        btnSaveGoal.setOnClickListener {
            saveGoal()
        }

        btnResetGoal.setOnClickListener {
            resetGoal()
        }
    }

    private fun saveGoal() {
        val user = auth.currentUser
        val goal = etGoal.text.toString().toDoubleOrNull()

        if (user != null && goal != null) {
            val goalData = hashMapOf(
                "userId" to user.uid,
                "goal" to goal
            )

            db.collection("carbonFootprintGoals")
                .document(user.uid)
                .set(goalData)
                .addOnSuccessListener {
                    with(sharedPreferences.edit()) {
                        putFloat("userGoal", goal.toFloat())
                        apply()
                    }
                    updateGoalDisplay(goal)
                    Toast.makeText(this, "Goal saved successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving goal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please enter a valid goal.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSavedGoal() {
        val savedGoal = sharedPreferences.getFloat("userGoal", 0f).toDouble()
        if (savedGoal > 0) {
            updateGoalDisplay(savedGoal)
        }
    }

    private fun updateGoalDisplay(goal: Double) {
        tvGoalDisplay.text = "Your goal: $goal kg CO2eq"

        val totalCarbonFootprint = sharedPreferences.getFloat("totalCarbonFootprint", 0f).toDouble()
        val remaining = goal - totalCarbonFootprint
        tvGoalLeft.text = "Carbon footprint left to achieve: ${remaining.coerceAtLeast(0.0)} kg CO2eq"
    }

    private fun resetGoal() {
        with(sharedPreferences.edit()) {
            remove("userGoal")
            apply()
        }
        tvGoalDisplay.text = "Your goal: Not set"
        tvGoalLeft.text = ""
        Toast.makeText(this, "Goal reset successfully!", Toast.LENGTH_SHORT).show()
    }
}
