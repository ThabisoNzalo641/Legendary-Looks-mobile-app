package com.example.bookingapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UpdateServiceActivity : AppCompatActivity() {

    private lateinit var db: com.google.firebase.firestore.FirebaseFirestore
    private var adminId: String = ""
    private var serviceCategory: String = ""
    private val servicesList = mutableListOf<Service>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.update_service_activity)

        // Initialize Firestore
        db = Firebase.firestore

        // Get admin data from intent
        adminId = intent.getStringExtra("ADMIN_ID") ?: ""
        serviceCategory = intent.getStringExtra("SERVICE_CATEGORY") ?: ""

        // Initialize views
        val serviceSpinner = findViewById<Spinner>(R.id.serviceSpinner)
        val serviceNameEditText = findViewById<EditText>(R.id.serviceNameEditText)
        val priceEditText = findViewById<EditText>(R.id.priceEditText)
        val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
        val updateButton = findViewById<Button>(R.id.updateButton)

        // Load services filtered by adminId
        loadServices(serviceSpinner, serviceNameEditText, priceEditText, descriptionEditText)

        serviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                if (position > 0) {
                    val selectedService = servicesList[position - 1]
                    serviceNameEditText.setText(selectedService.name)
                    priceEditText.setText(selectedService.price.toString())
                    descriptionEditText.setText(selectedService.description)
                } else {
                    serviceNameEditText.text.clear()
                    priceEditText.text.clear()
                    descriptionEditText.text.clear()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        updateButton.setOnClickListener {
            val selectedPosition = serviceSpinner.selectedItemPosition
            if (selectedPosition == 0) {
                Toast.makeText(this, "Please select a service to update", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedService = servicesList[selectedPosition - 1]
            val serviceName = serviceNameEditText.text.toString().trim()
            val price = priceEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()

            if (serviceName.isEmpty() || price.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateService(selectedService.serviceId, serviceName, price.toDouble(), description)
        }
    }

    private fun loadServices(spinner: Spinner, nameEditText: EditText, priceEditText: EditText, descEditText: EditText) {
        if (adminId.isEmpty()) {
            Toast.makeText(this, "Admin ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("services")
            .whereEqualTo("adminId", adminId) // Filter by adminId
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { documents ->
                servicesList.clear()
                for (document in documents) {
                    val service = document.toObject(Service::class.java)
                    service.serviceId = document.id
                    servicesList.add(service)
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
            }
    }

    private fun updateService(serviceId: String, name: String, price: Double, description: String) {
        val updates = hashMapOf<String, Any>(
            "name" to name,
            "price" to price,
            "description" to description
        )

        db.collection("services").document(serviceId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Service updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}