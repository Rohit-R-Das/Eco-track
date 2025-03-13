package com.example.ecotrack

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class RewardActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var badgeAnimationView: LottieAnimationView
    private lateinit var badgeTextView: TextView
    private lateinit var badgeMessageTextView: TextView
    private lateinit var praiseTextView: TextView
    private lateinit var improvementTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reward)

        // Initialize views
        badgeAnimationView = findViewById(R.id.badgeAnimationView)
        badgeTextView = findViewById(R.id.badgeTextView)
        badgeMessageTextView = findViewById(R.id.badgeMessageTextView)
        praiseTextView = findViewById(R.id.praiseTextView)
        improvementTextView = findViewById(R.id.improvementTextView)

        // Retrieve the total carbon footprint from shared preferences
        sharedPreferences = getSharedPreferences("EcoTrackPrefs", Context.MODE_PRIVATE)
        val totalCarbonFootprint = sharedPreferences.getFloat("totalCarbonFootprint", 0.0f)

        // Determine the badge and user title
        val badge = getBadgeForCarbonFootprint(totalCarbonFootprint)

        // Display the badge animation and name
        badgeAnimationView.setAnimation(badge.animationResId)
        badgeAnimationView.playAnimation()
        badgeTextView.text = badge.name

        // Display messages
        badgeMessageTextView.text = getBadgeMessage(totalCarbonFootprint, badge)
        praiseTextView.text = getPraiseMessage(badge)
        improvementTextView.text = getImprovementMessage(badge, totalCarbonFootprint)
    }

    private fun getBadgeForCarbonFootprint(totalCarbonFootprint: Float): Badge {
        return when {
            totalCarbonFootprint < 100 -> Badge("Bronze Badge - Eco Explorer", R.raw.bronze_badge_animation)
            totalCarbonFootprint < 200 -> Badge("Silver Badge - Eco Guardian", R.raw.silver_badge_animation)
            else -> Badge("Gold Badge - Eco Champion", R.raw.gold_trophy)
        }
    }

    private fun getBadgeMessage(totalCarbonFootprint: Float, badge: Badge): String {
        return when (badge.name) {
            "Bronze Badge - Eco Explorer" -> {
                val remainingForSilver = 100 - totalCarbonFootprint
                val remainingForGold = 200 - totalCarbonFootprint
                "You need $remainingForSilver more points to reach Silver and $remainingForGold more for Gold."
            }
            "Silver Badge - Eco Guardian" -> {
                val remainingForGold = 200 - totalCarbonFootprint
                "You need $remainingForGold more points to achieve the Gold Badge."
            }
            "Gold Badge - Eco Champion" -> "Congratulations! You have achieved the highest badge. Keep leading the way!"
            else -> ""
        }
    }

    private fun getPraiseMessage(badge: Badge): String {
        return when (badge.name) {
            "Bronze Badge - Eco Explorer" -> "Great start! You're on your way to making a difference. 🌱"
            "Silver Badge - Eco Guardian" -> "Amazing progress! Your dedication to sustainability is inspiring! 🌿"
            "Gold Badge - Eco Champion" -> "You're an Eco Champion! Your efforts are shaping a greener future! 🌎"
            else -> ""
        }
    }

    private fun getImprovementMessage(badge: Badge, totalCarbonFootprint: Float): String {
        return when (badge.name) {
            "Bronze Badge - Eco Explorer" -> "Try reducing waste and using public transport to level up faster!"
            "Silver Badge - Eco Guardian" -> "Encourage others to be eco-friendly and adopt more green habits!"
            "Gold Badge - Eco Champion" -> "Keep setting sustainability benchmarks and inspire more people!"
            else -> ""
        }
    }

    data class Badge(val name: String, val animationResId: Int)
}
