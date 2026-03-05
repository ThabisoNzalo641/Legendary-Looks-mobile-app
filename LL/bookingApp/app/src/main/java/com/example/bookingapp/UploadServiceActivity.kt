// UploadServiceActivity.kt
package com.example.bookingapp

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.Locale
import java.util.UUID

class UploadServiceActivity : AppCompatActivity() {

    private val TAG = "UploadServiceActivity"

    private lateinit var db: com.google.firebase.firestore.FirebaseFirestore
    private lateinit var storage: com.google.firebase.storage.FirebaseStorage

    private var adminId: String = ""
    private var serviceCategory: String = ""
    private var selectedImageUri: Uri? = null

    // Image picker
    private val imagePickerLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                if (isValidUri(selectedImageUri!!)) {
                    val serviceImageView = findViewById<ImageView>(R.id.serviceImageView)
                    val imageFileNameText = findViewById<TextView>(R.id.imageFileNameText)

                    Glide.with(this)
                        .load(selectedImageUri)
                        .into(serviceImageView)

                    val fileName = getFileName(selectedImageUri!!)
                    imageFileNameText.text = fileName
                    imageFileNameText.visibility = TextView.VISIBLE
                    Toast.makeText(this, "Image selected: $fileName", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Invalid image selected", Toast.LENGTH_SHORT).show()
                    selectedImageUri = null
                }
            }
        }

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_service_activity)

        // Firebase
        db = Firebase.firestore
        storage = Firebase.storage

        // Get adminId and category slug (CategoryActivity now sends normalized slug)
        adminId = intent.getStringExtra("ADMIN_ID") ?: ""
        serviceCategory = intent.getStringExtra("SERVICE_CATEGORY") ?: "" // legacy key
        // Also accept the CategoryActivity extra key if you used that constant
        if (serviceCategory.isEmpty()) {
            // Try alternate extra name used in earlier snippets
            serviceCategory = intent.getStringExtra(CategoryActivity.EXTRA_CATEGORY_NAME) ?: ""
        }

        // Views
        val serviceNameEditText = findViewById<EditText>(R.id.serviceNameEditText)
        val priceEditText = findViewById<EditText>(R.id.priceEditText)
        val durationEditText = findViewById<EditText>(R.id.durationEditText)
        val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
        val categoryTextView = findViewById<TextView>(R.id.categoryTextView)
        val uploadButton = findViewById<Button>(R.id.uploadButton)
        val selectImageButton = findViewById<Button>(R.id.selectImageButton)
        val serviceImageView = findViewById<ImageView>(R.id.serviceImageView)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val imageFileNameText = findViewById<TextView>(R.id.imageFileNameText)

        // Display category (if available)
        if (serviceCategory.isNotBlank()) {
            val display = serviceCategory.replaceFirstChar { it.uppercase(Locale.getDefault()) }
            categoryTextView.text = "Category: $display"
        } else {
            categoryTextView.text = "Category: (not specified)"
        }

        // Image selection
        selectImageButton.setOnClickListener { openImagePicker() }
        serviceImageView.setOnClickListener { openImagePicker() }

        // Upload button
        uploadButton.setOnClickListener {
            val serviceName = serviceNameEditText.text.toString().trim()
            val price = priceEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val duration = durationEditText.text.toString().trim()

            // Enhanced validation with detailed logging
            Log.d(TAG, "=== FORM VALIDATION ===")
            Log.d(TAG, "Service Name: '$serviceName'")
            Log.d(TAG, "Price: '$price'")
            Log.d(TAG, "Description: '$description'")
            Log.d(TAG, "Duration: '$duration'")
            Log.d(TAG, "Category: '$serviceCategory'")

            if (serviceName.isEmpty() || price.isEmpty() || description.isEmpty() || duration.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "❌ Validation failed: Empty fields detected")
                return@setOnClickListener
            }

            val priceValue = price.toDoubleOrNull()
            val durationValue = duration.toIntOrNull()

            if (priceValue == null || priceValue <= 0) {
                Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "❌ Validation failed: Invalid price '$price'")
                return@setOnClickListener
            }

            if (durationValue == null || durationValue <= 0) {
                Toast.makeText(this, "Please enter a valid duration", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "❌ Validation failed: Invalid duration '$duration'")
                return@setOnClickListener
            }

            // Validate category presence
            if (serviceCategory.isBlank()) {
                Toast.makeText(this, "Category not specified. Please open Upload from a category.", Toast.LENGTH_LONG).show()
                Log.e(TAG, "❌ Validation failed: No category specified")
                return@setOnClickListener
            }

            Log.d(TAG, "✅ All validation passed")
            Log.d(TAG, "Final values - Name: '$serviceName', Price: $priceValue, Duration: $durationValue, Desc: '$description'")

            // Disable UI while uploading
            uploadButton.isEnabled = false
            uploadButton.text = "Uploading..."
            progressBar.visibility = ProgressBar.VISIBLE

            // Start upload flow
            uploadService(serviceName, description, priceValue, durationValue)
        }
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun isValidUri(uri: Uri): Boolean {
        return try {
            contentResolver.openFileDescriptor(uri, "r")?.use {
                it.statSize > 0
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "URI validation failed: ${e.message}")
            false
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        result = it.getString(displayNameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "image_${System.currentTimeMillis()}.jpg"
    }

    private fun uploadService(serviceName: String, description: String, price: Double, duration: Int) {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val uploadButton = findViewById<Button>(R.id.uploadButton)

        // Normalize category slug (lowercase, underscores)
        val normalizedCategory = serviceCategory.trim().lowercase(Locale.getDefault()).replace("\\s+".toRegex(), "_")

        Log.d(TAG, "=== STARTING UPLOAD ===")
        Log.d(TAG, "Normalized Category: '$normalizedCategory'")

        if (selectedImageUri != null) {
            if (isValidUri(selectedImageUri!!)) {
                Log.d(TAG, "📸 Uploading with image")
                uploadImageThenService(normalizedCategory, serviceName, description, price, duration)
            } else {
                Toast.makeText(this, "Selected image is not accessible", Toast.LENGTH_SHORT).show()
                resetUploadButton()
                progressBar.visibility = ProgressBar.GONE
            }
        } else {
            Log.d(TAG, "📝 Uploading without image")
            // Confirm upload without image
            AlertDialog.Builder(this)
                .setTitle("No Image Selected")
                .setMessage("Do you want to continue without an image?")
                .setPositiveButton("Yes, Upload Without Image") { _, _ ->
                    uploadServiceToFirestore(normalizedCategory, serviceName, description, price, duration, null)
                }
                .setNegativeButton("Cancel") { _, _ ->
                    resetUploadButton()
                    progressBar.visibility = ProgressBar.GONE
                }
                .setCancelable(false)
                .show()
        }
    }

    // Uploads image and on success calls uploadServiceToFirestore(...)
    private fun uploadImageThenService(
        normalizedCategory: String,
        serviceName: String,
        description: String,
        price: Double,
        duration: Int
    ) {
        val imageUri = selectedImageUri ?: return
        val imageRef = storage.reference.child("service_images/${UUID.randomUUID()}_${adminId}.jpg")
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        Log.d(TAG, "🖼️ Starting image upload...")

        val uploadTask = imageRef.putFile(imageUri)
        uploadTask
            .addOnProgressListener { taskSnapshot ->
                val bytesTransferred = taskSnapshot.bytesTransferred
                val total = taskSnapshot.totalByteCount
                if (total > 0) {
                    val progress = (100.0 * bytesTransferred / total).toInt()
                    Log.d(TAG, "Image upload progress: $progress%")
                }
            }
            .addOnSuccessListener {
                Log.d(TAG, "✅ Image upload successful, getting download URL...")
                // get download URL
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Log.i(TAG, "📷 Image uploaded successfully. URL: $downloadUri")
                    uploadServiceToFirestore(normalizedCategory, serviceName, description, price, duration, downloadUri.toString())
                }.addOnFailureListener { e ->
                    Log.e(TAG, "❌ Failed to obtain download URL: ${e.message}", e)
                    Toast.makeText(this, "Upload succeeded but failed to read image URL: ${e.message}", Toast.LENGTH_LONG).show()
                    resetUploadButton()
                    progressBar.visibility = ProgressBar.GONE
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Image upload failed: ${e.message}", e)
                Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                resetUploadButton()
                progressBar.visibility = ProgressBar.GONE
            }
    }

    private fun uploadServiceToFirestore(
        normalizedCategory: String,
        serviceName: String,
        description: String,
        price: Double,
        duration: Int,
        imageUrl: String?
    ) {
        Log.d(TAG, "=== UPLOADING TO FIRESTORE ===")
        Log.d(TAG, "Service Name: '$serviceName'")
        Log.d(TAG, "Description: '$description'")
        Log.d(TAG, "Price: $price")
        Log.d(TAG, "Duration: $duration")
        Log.d(TAG, "Category: '$normalizedCategory'")
        Log.d(TAG, "Image URL: $imageUrl")
        Log.d(TAG, "Admin ID: $adminId")

        // Build the map with PROPER field names
        val serviceData = hashMapOf<String, Any>(
            "name" to serviceName,
            "description" to description,
            "price" to price,
            "duration" to duration,
            "category" to normalizedCategory,
            "adminId" to adminId,
            "isActive" to true,
            "imageUrl" to (imageUrl ?: ""),
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        Log.d(TAG, "📦 Final service data to upload: $serviceData")

        // Add document
        db.collection("services")
            .add(serviceData)
            .addOnSuccessListener { docRef ->
                Log.i(TAG, "🎉 SUCCESS: Service created with id ${docRef.id}")

                // Verify the upload by reading back the document
                docRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        Log.d(TAG, "✅ VERIFICATION: Document exists with data: ${document.data}")
                    } else {
                        Log.e(TAG, "❌ VERIFICATION: Document does not exist")
                    }
                }

                Toast.makeText(this, "Service uploaded successfully!", Toast.LENGTH_SHORT).show()
                clearForm()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ FAILED to upload service: ${e.message}", e)
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                resetUploadButton()
            }
    }

    private fun clearForm() {
        findViewById<EditText>(R.id.serviceNameEditText).text.clear()
        findViewById<EditText>(R.id.priceEditText).text.clear()
        findViewById<EditText>(R.id.durationEditText).text.clear()
        findViewById<EditText>(R.id.descriptionEditText).text.clear()

        val serviceImageView = findViewById<ImageView>(R.id.serviceImageView)
        serviceImageView.setImageDrawable(null)
        serviceImageView.setBackgroundColor(resources.getColor(android.R.color.darker_gray, theme))

        findViewById<TextView>(R.id.imageFileNameText).visibility = TextView.GONE
        selectedImageUri = null
    }

    private fun resetUploadButton() {
        val uploadButton = findViewById<Button>(R.id.uploadButton)
        uploadButton.isEnabled = true
        uploadButton.text = "Upload Service"
        findViewById<ProgressBar>(R.id.progressBar)?.visibility = ProgressBar.GONE
    }
}