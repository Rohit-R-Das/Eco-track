package com.example.ecotrack

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ecotrack.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth  // ✅ Declare FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        supportActionBar?.title = "Register"
        auth = FirebaseAuth.getInstance()  // ✅ Initialize FirebaseAuth

        // Navigate to LoginActivity when "Login" is clicked
        binding.loginTV.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Adjust window insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handle account creation
        binding.createAccountBtn.setOnClickListener {
            val email = binding.emailRegister.text.toString().trim()
            val password = binding.passwordRegister.text.toString().trim()

            if (validateInput(email, password)) {
                registerUser(email, password)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        return when {
            email.isEmpty() || password.isEmpty() -> {
                showToast("Email and password must not be empty")
                false
            }
            !emailRegex.matches(email) -> {
                showToast("Please enter a valid email address")
                false
            }
            password.length < 8 || !password.matches(Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,}$")) -> {
                showToast("Password must be at least 8 characters long and include a letter, a number, and a special character")
                false
            }
            else -> true
        }
    }



    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Registration successful! Redirecting to login...")

                    // Sign out the user so they are not automatically logged in
                    auth.signOut()

                    // Navigate to LoginActivity
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    showToast("Registration failed: ${task.exception?.localizedMessage}")
                }
            }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
