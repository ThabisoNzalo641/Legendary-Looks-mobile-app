package com.example.bookingapp

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DeleteServiceActivity : AppCompatActivity() {

    private lateinit var db: com.google.firebase.firestore.FirebaseFirestore
    private var adminId: String = ""
    private val servicesList = mutableListOf<Service>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.delete_service_activity)

        // Initialize Firestore
        db = Firebase.firestore

        // Get admin data from intent
        adminId = intent.getStringExtra("ADMIN_ID") ?: ""

        // Initialize views
        val serviceSpinner = findViewById<Spinner>(R.id.serviceSpinner)
        val deleteButton = findViewById<Button>(R.id.deleteButton)

        // Load services filtered by adminId
        loadServices(serviceSpinner)

        deleteButton.setOnClickListener {
            val selectedPosition = serviceSpinner.selectedItemPosition
            if (selectedPosition == 0) {
                Toast.makeText(this, "Please select a service to delete", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedService = servicesList[selectedPosition - 1]
            showDeleteConfirmation(selectedService)
        }
    }

    private fun loadServices(spinner: Spinner) {
        if (adminId.isEmpty()) {
            Toast.makeText(this, "Admin ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("DeleteService", "🔄 Loading services for admin: $adminId")

        db.collection("services")
            .whereEqualTo("adminId", adminId)
            .get()
            .addOnSuccessListener { documents ->
                servicesList.clear()
                for (document in documents) {
                    val service = document.toObject(Service::class.java)
                    service.serviceId = document.id
                    servicesList.add(service)
                    Log.d("DeleteService", "📋 Loaded service: ${service.name} (ID: ${document.id})")
                }

                val serviceNames = mutableListOf("Select Service")
                serviceNames.addAll(servicesList.map { it.name })

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, serviceNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                if (servicesList.isEmpty()) {
                    Toast.makeText(this, "No services found for your business", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading services: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("DeleteService", "❌ Error loading services: ${e.message}")
            }
    }

    private fun showDeleteConfirmation(service: Service) {
        AlertDialog.Builder(this)
            .setTitle("Permanently Delete Service")
            .setMessage("Are you sure you want to permanently delete '${service.name}'? This will remove it from the database completely.")
            .setPositiveButton("Delete") { dialog, which ->
                deleteServiceFromFirestore(service)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteServiceFromFirestore(service: Service) {
        val serviceId = service.serviceId
        val serviceName = service.name

        Log.d("DeleteService", "🗑️ Attempting to delete service: $serviceName (ID: $serviceId)")

        // METHOD 1: Direct deletion
        db.collection("services").document(serviceId)
            .delete()
            .addOnSuccessListener {
                Log.d("DeleteService", "✅ SUCCESS: Service $serviceName (ID: $serviceId) deleted from Firestore")
                Toast.makeText(this, "Service '$serviceName' permanently deleted!", Toast.LENGTH_SHORT).show()

                // Refresh the spinner
                val serviceSpinner = findViewById<Spinner>(R.id.serviceSpinner)
                loadServices(serviceSpinner)
            }
            .addOnFailureListener { e ->
                Log.e("DeleteService", "❌ FAILED to delete service: ${e.message}")
                Toast.makeText(this, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()

                // Try alternative method if first fails
                tryAlternativeDelete(serviceId, serviceName)
            }
    }

    private fun tryAlternativeDelete(serviceId: String, serviceName: String) {
        Log.d("DeleteService", "🔄 Trying alternative deletion method...")

        // METHOD 2: Try with a new document reference
        val serviceRef = db.collection("services").document(serviceId)

        serviceRef.delete()
            .addOnSuccessListener {
                Log.d("DeleteService", "✅ ALTERNATIVE SUCCESS: Service deleted")
                Toast.makeText(this, "Service '$serviceName' deleted!", Toast.LENGTH_SHORT).show()
                val serviceSpinner = findViewById<Spinner>(R.id.serviceSpinner)
                loadServices(serviceSpinner)
            }
            .addOnFailureListener { e ->
                Log.e("DeleteService", "❌ ALTERNATIVE FAILED: ${e.message}")
                Toast.makeText(this, "Delete failed completely: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}