package com.example.bookingapp;

import com.google.firebase.Timestamp;

public class Appointment {
    private String bookingId;
    private String serviceName;
    private String serviceCategory;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String serviceId;
    private String specialRequests;
    private String bookingDate;
    private String bookingTime;
    private String status;
    private double totalPrice;
    private Timestamp updatedAt; // Changed from String to Timestamp
    private Timestamp createdAt; // Changed from String to Timestamp
    private String userId;
    private String adminId;
    private long duration; // Changed from int to long

    // Default constructor required for Firestore
    public Appointment() {}

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getServiceCategory() { return serviceCategory; }
    public void setServiceCategory(String serviceCategory) { this.serviceCategory = serviceCategory; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getBookingTime() { return bookingTime; }
    public void setBookingTime(String bookingTime) { this.bookingTime = bookingTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    // Helper method to get formatted date and time
    public String getDateTime() {
        if (bookingDate != null && bookingTime != null) {
            return bookingDate + " • " + bookingTime;
        } else if (bookingDate != null) {
            return bookingDate;
        } else if (bookingTime != null) {
            return bookingTime;
        } else {
            return "Date/Time not specified";
        }
    }

    // Helper method to get formatted status
    public String getFormattedStatus() {
        if (status == null) return "Unknown";

        switch (status.toLowerCase()) {
            case "pending":
                return "⏳ Pending";
            case "confirmed":
                return "✅ Confirmed";
            case "completed":
                return "🎉 Completed";
            case "cancelled":
                return "❌ Cancelled";
            default:
                return status;
        }
    }

    // Helper method to get formatted updated date
    public String getFormattedUpdatedDate() {
        if (updatedAt != null) {
            // Convert timestamp to readable date
            return updatedAt.toDate().toString();
        }
        return "Date not available";
    }
}