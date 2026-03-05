package com.example.bookingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSignUp: Button
    private lateinit var tvSignUpLink: TextView
    private lateinit var progressBar: ProgressBar

    // New UI for strength indicator
    private lateinit var tvPasswordStrength: TextView
    private lateinit var pbPasswordStrength: ProgressBar
    private lateinit var tvPasswordGuidelines: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Initialize views with new IDs from the updated XML
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnSignUp = findViewById(R.id.btn_sign_up)
        tvSignUpLink = findViewById(R.id.tv_sign_up_link)
        progressBar = findViewById(R.id.progressBar)

        // New views
        tvPasswordStrength = findViewById(R.id.tv_password_strength)
        pbPasswordStrength = findViewById(R.id.pb_password_strength)
        tvPasswordGuidelines = findViewById(R.id.tv_password_guidelines)

        // Setup TextWatcher for real-time password strength
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* no-op */ }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /* no-op */ }

            override fun afterTextChanged(s: Editable?) {
                val pwd = s?.toString() ?: ""
                updatePasswordStrengthUI(pwd)
            }
        })

        btnSignUp.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate password strength (same rules as before)
            if (!isStrongPassword(password)) {
                Toast.makeText(
                    this,
                    "Password must contain:\n• At least 1 capital letter\n• At least 1 number\n• At least 1 special character\n• Minimum 6 characters",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            registerUser(email, password)
        }

        tvSignUpLink.setOnClickListener {
            finish() // Go back to login
        }
    }

    /**
     * Update the strength TextView + progress bar in real-time.
     * Rules used:
     * - Weak: length < 6 or only one character class present
     * - Medium: length >= 6 and two character classes present
     * - Strong: length >= 8 and at least three character classes present (upper, digit, special)
     */
    private fun updatePasswordStrengthUI(password: String) {
        val hasUpper = Regex(".*[A-Z].*").containsMatchIn(password)
        val hasLower = Regex(".*[a-z].*").containsMatchIn(password)
        val hasDigit = Regex(".*[0-9].*").containsMatchIn(password)
        val hasSpecial = Regex(".*[!@#\$%^&*()_+=<>?/{}|~`\\-].*").containsMatchIn(password)

        val classes = listOf(hasUpper, hasLower, hasDigit, hasSpecial).count { it }
        val length = password.length

        when {
            password.isEmpty() -> {
                tvPasswordStrength.text = ""
                pbPasswordStrength.progress = 0
                tvPasswordGuidelines.visibility = View.VISIBLE
            }
            length < 6 || classes <= 1 -> { // Weak
                tvPasswordStrength.text = "Weak"
                tvPasswordStrength.setTextColor(Color.parseColor("#D32F2F")) // red
                pbPasswordStrength.progress = 25
                tvPasswordGuidelines.visibility = View.VISIBLE
            }
            length >= 6 && classes == 2 -> { // Medium
                tvPasswordStrength.text = "Medium"
                tvPasswordStrength.setTextColor(Color.parseColor("#F9A825")) // amber
                pbPasswordStrength.progress = 60
                tvPasswordGuidelines.visibility = View.VISIBLE
            }
            length >= 8 && classes >= 3 -> { // Strong
                tvPasswordStrength.text = "Strong"
                tvPasswordStrength.setTextColor(Color.parseColor("#2E7D32")) // green
                pbPasswordStrength.progress = 100
                tvPasswordGuidelines.visibility = View.GONE
            }
            else -> { // default fallback to Medium-style
                tvPasswordStrength.text = "Medium"
                tvPasswordStrength.setTextColor(Color.parseColor("#F9A825"))
                pbPasswordStrength.progress = 60
                tvPasswordGuidelines.visibility = View.VISIBLE
            }
        }
    }

    // Password Validation Function (same as before)
    private fun isStrongPassword(password: String): Boolean {
        val capitalLetter = Regex(".*[A-Z].*")
        val number = Regex(".*[0-9].*")
        val special = Regex(".*[!@#\$%^&*()_+=<>?/{}|~`\\-].*")

        return password.length >= 6 &&
                capitalLetter.containsMatchIn(password) &&
                number.containsMatchIn(password) &&
                special.containsMatchIn(password)
    }

    private fun registerUser(email: String, password: String) {
        progressBar.visibility = View.VISIBLE
        btnSignUp.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                btnSignUp.isEnabled = true

                if (task.isSuccessful) {
                    // Registration success
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

                    // Navigate back to MainActivity (login screen)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Registration failed
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
