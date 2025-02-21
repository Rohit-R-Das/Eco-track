package com.example.ecotrack

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var carbonFootprintTextView: TextView

    private val REQUEST_IMAGE_PICK = 101
    private val PREFS_NAME = "EcoTrackPrefs"
    private val PROFILE_IMAGE_KEY = "profileImage"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize views
        profileImageView = findViewById(R.id.profileImageView)
        userNameTextView = findViewById(R.id.userNameTextView)
        userEmailTextView = findViewById(R.id.userEmailTextView)
        carbonFootprintTextView = findViewById(R.id.carbonFootprintTextView)

        // Load profile image from SharedPreferences
        loadProfileImage()

        // Load user details from Google Sign-In or Firebase
        loadUserDetails()

        // Load total carbon footprint from SharedPreferences
        loadTotalCarbonFootprint()

        // Set up profile image upload & remove option
        profileImageView.setOnClickListener {
            showImageOptions()
        }
    }

    private fun loadUserDetails() {
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (account != null) {
            userNameTextView.text = "Name: ${account.displayName}"
            userEmailTextView.text = "Email: ${account.email}"
        } else if (firebaseUser != null) {
            userNameTextView.text = "Name: ${firebaseUser.displayName ?: "N/A"}"
            userEmailTextView.text = "Email: ${firebaseUser.email}"
        } else {
            userNameTextView.text = "Name: Not Available"
            userEmailTextView.text = "Email: Not Available"
            Toast.makeText(this, "No user data found", Toast.LENGTH_LONG).show()
        }
    }

    private fun showImageOptions() {
        val options = arrayOf("Choose from Gallery", "Remove Profile Picture")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Profile Picture")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> pickImageFromGallery()
                1 -> removeProfileImage()
            }
        }
        builder.show()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun removeProfileImage() {
        profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(PROFILE_IMAGE_KEY).apply()
        Toast.makeText(this, "Profile picture removed", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                profileImageView.setImageBitmap(bitmap)
                saveProfileImage(bitmap)
            }
        }
    }

    private fun saveProfileImage(bitmap: Bitmap) {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        val encodedImage = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
        editor.putString(PROFILE_IMAGE_KEY, encodedImage)
        editor.apply()
    }

    private fun loadProfileImage() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encodedImage = sharedPreferences.getString(PROFILE_IMAGE_KEY, null)

        if (encodedImage != null) {
            val byteArray = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            profileImageView.setImageBitmap(bitmap)
        }
    }

    private fun loadTotalCarbonFootprint() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val totalCarbonFootprint = sharedPreferences.getFloat("totalCarbonFootprint", 0.0f)
        carbonFootprintTextView.text = "Carbon Footprint: $totalCarbonFootprint kg CO2eq"
    }
}