package com.example.ecotrack

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ecotrack.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Check if user is already logged in (Firebase email/password OR Google sign-in)
        val user = auth.currentUser
        if (user != null) {
            Log.d("MainActivity", "User Logged In: ${user.email} (Provider: ${user.providerId})")

            // Check authentication providers
            for (profile in user.providerData) {
                Log.d("MainActivity", "User Provider: ${profile.providerId}")
            }

            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Ensure correct redirection for Sign-In
        binding.signIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java)) // Fixed
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        binding.userDetails.text = updateData()
    }

    private fun updateData(): String {
        return "Know your Share in world Carbon footprint!"
    }
}
