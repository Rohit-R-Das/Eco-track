package com.example.ecotrack

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class NameActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name)

        val nameEditText: EditText = findViewById(R.id.et_name)
        val submitButton: Button = findViewById(R.id.btn_submit)

        submitButton.setOnClickListener {
            val name = nameEditText.text.toString()
            if (name.isNotEmpty()) {
                // Save the user's name in SharedPreferences
                val sharedPreferences = getSharedPreferences("EcoTrackPrefs", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putString("USER_NAME", name)
                    apply()
                }

                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                nameEditText.error = "Please enter your name"
            }
        }
    }
}