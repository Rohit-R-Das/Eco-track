package com.example.ecotrack

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Insight : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insight)

        barChart = findViewById(R.id.chart)
        pieChart = findViewById(R.id.pieChart)

        // Set background color for better UI
        barChart.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_color))
        pieChart.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_color))

        // Get logged-in user ID and fetch data
        auth.currentUser?.let {
            fetchCarbonFootprintData(it.uid)
        } ?: Log.e("Firestore", "User not logged in")
    }

    private fun fetchCarbonFootprintData(userId: String) {
        firestore.collection("carbonFootprints")
            .whereEqualTo("userId", userId) // Query only the logged-in user's data
            .get()
            .addOnSuccessListener { documents ->
                val categoryMap = mutableMapOf(
                    "Commute" to 0f,
                    "Household" to 0f,
                    "Food" to 0f,
                    "Waste" to 0f
                )

                for (document in documents) {
                    val footprint = document.getDouble("carbonFootprint")?.toFloat() ?: 0f
                    val activityType = document.getString("activityType") ?: ""

                    if (activityType in categoryMap) {
                        categoryMap[activityType] = categoryMap[activityType]!! + footprint
                    }
                }

                updateBarChart(categoryMap)
                updatePieChart(categoryMap)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching data", e)
            }
    }

    private fun updateBarChart(categoryMap: Map<String, Float>) {
        val entries = ArrayList<BarEntry>()
        val labels = arrayOf("Travel", "Energy", "Shopping", "Waste")

        categoryMap.values.forEachIndexed { index, value ->
            entries.add(BarEntry(index.toFloat(), value))
        }

        val dataSet = BarDataSet(entries, "Carbon Footprint (kg CO₂e)").apply {
            colors = listOf(
                ContextCompat.getColor(this@Insight, R.color.travelColor),
                ContextCompat.getColor(this@Insight, R.color.energyColor),
                ContextCompat.getColor(this@Insight, R.color.shoppingColor),
                ContextCompat.getColor(this@Insight, R.color.wasteColor)
            )
            valueTextColor = Color.BLACK
            valueTextSize = 14f
        }

        val barData = BarData(dataSet).apply { barWidth = 0.9f }

        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            textSize = 14f
            labelRotationAngle = -45f
            granularity = 1f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return labels.getOrNull(value.toInt()) ?: ""
                }
            }
        }

        barChart.apply {
            axisLeft.textSize = 14f
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = true
            setFitBars(true)
            animateY(1000)
            data = barData
            invalidate()
        }
    }


    private fun updatePieChart(categoryMap: Map<String, Float>) {
        val entries = mutableListOf<PieEntry>()

        if (categoryMap["Commute"]!! > 0) entries.add(PieEntry(categoryMap["Commute"]!!, "Travel"))
        if (categoryMap["Household"]!! > 0) entries.add(PieEntry(categoryMap["Household"]!!, "Energy"))
        if (categoryMap["Food"]!! > 0) entries.add(PieEntry(categoryMap["Food"]!!, "Shopping"))
        if (categoryMap["Waste"]!! > 0) entries.add(PieEntry(categoryMap["Waste"]!!, "Waste"))

        val dataSet = PieDataSet(entries, "Carbon Footprint").apply {
            colors = listOf(
                ContextCompat.getColor(this@Insight, R.color.travelColor),
                ContextCompat.getColor(this@Insight, R.color.energyColor),
                ContextCompat.getColor(this@Insight, R.color.shoppingColor),
                ContextCompat.getColor(this@Insight, R.color.wasteColor)
            )

            // Enable value lines (Arrows) for labels outside
            setDrawValues(true)
            sliceSpace = 2f
            valueTextSize = 14f
            valueTextColor = Color.BLACK
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE  // Labels outside
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE  // Labels outside

            // Configure line settings for better appearance
            valueLinePart1OffsetPercentage = 80f  // Offset from the center
            valueLinePart1Length = 0.4f  // First segment of the line
            valueLinePart2Length = 0.4f  // Second segment of the line
            valueLineColor = Color.BLACK  // Line color
        }

        val pieData = PieData(dataSet)

        pieChart.apply {
            data = pieData
            setUsePercentValues(true)
            setDrawHoleEnabled(true)
            holeRadius = 40f
            transparentCircleRadius = 50f
            setEntryLabelColor(Color.BLACK)  // Label color
            setEntryLabelTextSize(12f)  // Label text size
            setExtraOffsets(20f, 10f, 20f, 10f)  // Space for labels outside
            description.isEnabled = false
            legend.isEnabled = true
            animateY(1000)
            invalidate()
        }
    }

}
