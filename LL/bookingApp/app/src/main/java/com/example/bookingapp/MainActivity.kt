package com.example.bookingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: com.google.firebase.firestore.FirebaseFirestore
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var noAccountTextView: TextView
    private lateinit var progressBar: ProgressBar

    // List of admin emails
    private val adminEmails = listOf(
        "lashesbylisa@gmail.com",
        "eviienails@gmail.com",
        "braidedbynerry@gmail.com"
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase
        auth = Firebase.auth
        db = Firebase.firestore

        // Initialize views FIRST
        initializeViews()

        // Check if user is already logged in
        if (auth.currentUser != null) {
            checkUserAndNavigate()
            return
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        noAccountTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initializeViews() {
        emailEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        noAccountTextView = findViewById(R.id.tv_sign_up_link)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun loginUser(email: String, password: String) {
        progressBar.visibility = View.VISIBLE
        loginButton.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login success - check email and navigate
                    checkUserAndNavigate()
                } else {
                    hideProgress()
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkUserAndNavigate() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userEmail = currentUser.email ?: ""

            // Check if the email is one of the admin emails
            if (adminEmails.contains(userEmail.toLowerCase())) {
                // Admin user - navigate to DashboardActivity
                println("DEBUG: Admin email detected - $userEmail")
                navigateToDashboard(currentUser.uid, userEmail)
            } else {
                // Regular user - navigate to CategoryActivity
                println("DEBUG: Regular user email - $userEmail")
                navigateToCategory(currentUser.uid)
            }
        } else {
            hideProgress()
        }
    }

    private fun navigateToDashboard(adminId: String, email: String) {
        try {
            val serviceCategory = getServiceCategoryFromEmail(email)
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("ADMIN_ID", adminId)
            intent.putExtra("SERVICE_CATEGORY", serviceCategory)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error navigating to dashboard: ${e.message}", Toast.LENGTH_SHORT).show()
            hideProgress()
        }
    }

    private fun navigateToCategory(userId: String) {
        try {
            val intent = Intent(this, CategoryActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: CategoryActivity not found. Please check AndroidManifest.xml", Toast.LENGTH_LONG).show()
            // Fallback - sign out and show login again
            auth.signOut()
            hideProgress()
        }
    }

    private fun hideProgress() {
        progressBar.visibility = View.GONE
        loginButton.isEnabled = true
    }

    private fun getServiceCategoryFromEmail(email: String): String {
        return when (email.toLowerCase()) {
            "lashesbylisa@gmail.com" -> "lashes"
            "eviienails@gmail.com" -> "nails"
            "braidedbynerry@gmail.com" -> "hair"
            else -> "general"
        }
    }
}