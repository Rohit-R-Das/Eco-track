package com.example.ecotrack

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SavingTargetActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var etGoal: EditText
    private lateinit var toggleGoalType: ToggleButton
    private lateinit var calendarView: CalendarView
    private lateinit var btnSaveGoal: Button
    private lateinit var pieChart: PieChart
    private lateinit var btnResetGoal: Button
    private lateinit var tvGoalStatus: TextView

    private var goalValue: Double = 0.0
    private var goalType: String = "Weekly"
    private var progress: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saving_target)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etGoal = findViewById(R.id.etGoal)
        toggleGoalType = findViewById(R.id.toggleGoalType)
        calendarView = findViewById(R.id.calendarView)
        btnSaveGoal = findViewById(R.id.btnSaveGoal)
        pieChart = findViewById(R.id.pieChart)
        btnResetGoal = findViewById(R.id.btnResetGoal)
        tvGoalStatus = findViewById(R.id.tvGoalStatus)

        loadGoalFromFirestore()

        toggleGoalType.setOnCheckedChangeListener { _, isChecked ->
            goalType = if (isChecked) "Monthly" else "Weekly"
            loadGoalFromFirestore()
        }

        btnSaveGoal.setOnClickListener {
            saveGoalToFirestore()
        }

        btnResetGoal.setOnClickListener {
            resetGoal()
        }

        setupPieChart()
    }

    private fun setupPieChart() {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.isRotationEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)
    }

    private fun updatePieChart(achieved: Float, remaining: Float) {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(achieved, "Achieved"))
        entries.add(PieEntry(remaining, "Remaining"))

        val dataSet = PieDataSet(entries, "Goal Status")
        dataSet.colors = listOf(Color.GREEN, Color.RED)
        dataSet.valueTextSize = 14f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate()  // Refresh the chart
    }

    private fun loadGoalFromFirestore() {
        val userId = auth.currentUser?.uid ?: "defaultUser"
        db.collection("carbonFootprintGoals").document("$userId-$goalType")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    goalValue = document.getDouble("goalValue") ?: 0.0
                    progress = getSharedPreferences("EcoTrackPrefs", Context.MODE_PRIVATE)
                        .getFloat("totalCarbonFootprint", 0.0f).toDouble()
                    updateGoalStatus()
                } else {
                    Toast.makeText(this, "No goal data found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load goal data!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveGoalToFirestore() {
        goalValue = etGoal.text.toString().toDoubleOrNull() ?: 0.0
        val userId = auth.currentUser?.uid ?: "defaultUser"

        if (goalValue > 0) {
            val goalData = hashMapOf(
                "goalType" to goalType,
                "goalValue" to goalValue,
                "progress" to progress
            )

            db.collection("carbonFootprintGoals").document("$userId-$goalType")
                .set(goalData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Goal saved successfully!", Toast.LENGTH_SHORT).show()
                    updateGoalStatus()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save goal!", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Enter a valid goal", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetGoal() {
        val userId = auth.currentUser?.uid ?: "defaultUser"
        db.collection("carbonFootprintGoals").document("$userId-$goalType")
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "$goalType goal reset successfully!", Toast.LENGTH_SHORT).show()
                updatePieChart(0f, 100f)
                tvGoalStatus.text = "$goalType Goal Status: No goal set"
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to reset goal!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateGoalStatus() {
        val remaining = goalValue - progress
        val achievedPercentage = ((progress / goalValue) * 100).toFloat()
        val remainingPercentage = 100 - achievedPercentage

        updatePieChart(achievedPercentage, remainingPercentage)
        tvGoalStatus.text = "$goalType Goal Value: $goalValue kg CO2eq\nRemaining: ${String.format("%.2f", remaining)} kg CO2eq"
    }
}
