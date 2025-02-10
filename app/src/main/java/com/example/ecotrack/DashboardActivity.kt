package com.example.ecotrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve the user's name from SharedPreferences
        val sharedPreferences = getSharedPreferences("EcoTrackPrefs", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("USER_NAME", "User")

        val welcomeTextView: TextView = findViewById(R.id.tv_welcome)
        welcomeTextView.text = "Hi $userName, welcome to Eco Track app!"

        val commuteButton: Button = findViewById(R.id.commuteButton)
        commuteButton.setOnClickListener {
            startActivity(Intent(this, CommuteActivity::class.java))
        }

        val householdButton: Button = findViewById(R.id.householdButton)
        householdButton.setOnClickListener {
            startActivity(Intent(this, HouseholdActivity::class.java))
        }

        val foodButton: Button = findViewById(R.id.foodButton)
        foodButton.setOnClickListener {
            startActivity(Intent(this, FoodActivity::class.java))
        }

        val signOutButton: Button = findViewById(R.id.signOut)
        signOutButton.setOnClickListener {
            auth.signOut()
            googleSignInClient.signOut().addOnCompleteListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}