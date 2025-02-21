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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reward)

        // Initialize views
        badgeAnimationView = findViewById(R.id.badgeAnimationView)
        badgeTextView = findViewById(R.id.badgeTextView)
        badgeMessageTextView = findViewById(R.id.badgeMessageTextView)

        // Retrieve the total carbon footprint from shared preferences
        sharedPreferences = getSharedPreferences("EcoTrackPrefs", Context.MODE_PRIVATE)
        val totalCarbonFootprint = sharedPreferences.getFloat("totalCarbonFootprint", 0.0f)

        // Determine the badge to award based on the total carbon footprint
        val badge = getBadgeForCarbonFootprint(totalCarbonFootprint)

        // Display the badge animation
        badgeAnimationView.setAnimation(badge.animationResId)
        badgeAnimationView.playAnimation()
        badgeTextView.text = badge.name

        // Display the message for the badge holder
        badgeMessageTextView.text = getBadgeMessage(totalCarbonFootprint, badge)
    }

    private fun getBadgeForCarbonFootprint(totalCarbonFootprint: Float): Badge {
        return when {
            totalCarbonFootprint < 100 -> Badge("Bronze Badge", R.raw.bronze_badge_animation)
            totalCarbonFootprint < 200 -> Badge("Silver Badge", R.raw.silver_badge_animation)
            else -> Badge("Gold Badge", R.raw.gold_trophy)
        }
    }

    private fun getBadgeMessage(totalCarbonFootprint: Float, badge: Badge): String {
        return when (badge.name) {
            "Bronze Badge" -> {
                val remainingForSilver = 100 - totalCarbonFootprint
                val remainingForGold = 200 - totalCarbonFootprint
                "You need $remainingForSilver more to get the Silver Badge and $remainingForGold more to get the Gold Badge."
            }
            "Silver Badge" -> {
                val remainingForGold = 200 - totalCarbonFootprint
                "You need $remainingForGold more to get the Gold Badge."
            }
            "Gold Badge" -> "Congratulations! You have achieved the highest badge."
            else -> ""
        }
    }

    data class Badge(val name: String, val animationResId: Int)
}