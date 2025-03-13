package com.example.ecotrack

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)

        // Retrieve the user's name from SharedPreferences
        val sharedPreferences = getSharedPreferences("EcoTrackPrefs", Context.MODE_PRIVATE)
        var userName = sharedPreferences.getString("USER_NAME", "User")

        // Retrieve the user's name from Google Sign-In
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            userName = account.displayName
        }

        // Set the welcome message
        val welcomeTextView: TextView = findViewById(R.id.tv_welcome)
        welcomeTextView.text = "Hi $userName, welcome to EcoTrack!"



        // CardView Click Listeners
        findViewById<CardView>(R.id.card_commute).setOnClickListener {
            startActivity(Intent(this, CommuteActivity::class.java))
        }

        findViewById<CardView>(R.id.card_energy).setOnClickListener {
            startActivity(Intent(this, HouseholdActivity::class.java))
        }

        findViewById<CardView>(R.id.shopping_card).setOnClickListener {
            startActivity(Intent(this, FoodActivity::class.java))
        }

        findViewById<CardView>(R.id.waste_card).setOnClickListener {
            startActivity(Intent(this, WasteActivity::class.java))
        }

        findViewById<CardView>(R.id.saving_history).setOnClickListener {
            startActivity(Intent(this, SavingHistoryActivity::class.java))
        }
        findViewById<CardView>(R.id.Target_card).setOnClickListener {
            startActivity(Intent(this, SavingTargetActivity::class.java))
        }
        findViewById<CardView>(R.id.Reward_card).setOnClickListener {
            startActivity(Intent(this, RewardActivity::class.java))
        }

        findViewById<CardView>(R.id.profile_card).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<CardView>(R.id.card_insight).setOnClickListener {
            startActivity(Intent(this, Insight::class.java))
        }

        findViewById<CardView>(R.id.card_tips).setOnClickListener {
            startActivity(Intent(this, Tips::class.java))
        }

        // Sign Out Button Click Listener
        findViewById<Button>(R.id.signOut).setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}