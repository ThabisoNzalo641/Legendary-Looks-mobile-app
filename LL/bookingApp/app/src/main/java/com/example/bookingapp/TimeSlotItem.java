package com.example.bookingapp;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TimeSlotItem {

    private final String timeSlot;
    private final String grouping;
    private boolean isAvailable;
    private final String date;
    private String bookingId;

    public TimeSlotItem(String timeSlot, String grouping, boolean isAvailable, String date) {
        this.timeSlot = timeSlot;
        this.grouping = grouping;
        this.isAvailable = isAvailable;
        this.date = date;
        this.bookingId = null;
    }

    public TimeSlotItem(String timeSlot, String grouping, boolean isAvailable) {
        this.timeSlot = timeSlot;
        this.grouping = grouping;
        this.isAvailable = isAvailable;
        this.date = "";
        this.bookingId = null;
    }

    public String getTimeSlot() { return timeSlot; }
    public String getGrouping() { return grouping; }
    public boolean isAvailable() { return isAvailable; }
    public String getDate() { return date; }
    public String getBookingId() { return bookingId; }

    public void setAvailable(boolean available) { isAvailable = available; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public static void checkTimeSlotsAvailability(List<TimeSlotItem> timeSlots, String serviceCategory,
                                                  AvailabilityCheckCallback callback) {
        if (timeSlots == null || timeSlots.isEmpty()) {
            if (callback != null) callback.onAvailabilityChecked(timeSlots);
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<TimeSlotItem> updatedTimeSlots = new ArrayList<>(timeSlots);
        AtomicInteger checksCompleted = new AtomicInteger(0);
        int totalChecks = timeSlots.size();

        for (TimeSlotItem timeSlot : timeSlots) {
            if (timeSlot.getDate() == null || timeSlot.getDate().isEmpty()) {
                checksCompleted.incrementAndGet();
                continue;
            }

            String normalizedCategory = normalizeCategory(serviceCategory);

            db.collection("bookings")
                    .whereEqualTo("bookingDate", timeSlot.getDate())
                    .whereEqualTo("bookingTime", timeSlot.getTimeSlot())
                    .whereEqualTo("serviceCategory", normalizedCategory)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        boolean isBooked = false;
                        String existingBookingId = null;

                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String status = document.getString("status");
                            if (status != null &&
                                    (status.equals("pending") || status.equals("confirmed") || status.equals("in_progress"))) {
                                isBooked = true;
                                existingBookingId = document.getId();
                                break;
                            }
                        }

                        for (TimeSlotItem slot : updatedTimeSlots) {
                            if (slot.getTimeSlot().equals(timeSlot.getTimeSlot()) &&
                                    slot.getDate().equals(timeSlot.getDate())) {
                                slot.setAvailable(!isBooked);
                                if (isBooked) {
                                    slot.setBookingId(existingBookingId);
                                }
                                break;
                            }
                        }

                        int completed = checksCompleted.incrementAndGet();
                        if (completed >= totalChecks && callback != null) {
                            callback.onAvailabilityChecked(updatedTimeSlots);
                        }
                    })
                    .addOnFailureListener(e -> {
                        int completed = checksCompleted.incrementAndGet();
                        if (completed >= totalChecks && callback != null) {
                            callback.onAvailabilityChecked(updatedTimeSlots);
                        }
                    });
        }
    }

    public static void checkSingleTimeSlotAvailability(String date, String time, String serviceCategory,
                                                       SingleAvailabilityCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String normalizedCategory = normalizeCategory(serviceCategory);

        db.collection("bookings")
                .whereEqualTo("bookingDate", date)
                .whereEqualTo("bookingTime", time)
                .whereEqualTo("serviceCategory", normalizedCategory)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean isAvailable = true;

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String status = document.getString("status");
                        if (status != null &&
                                (status.equals("pending") || status.equals("confirmed") || status.equals("in_progress"))) {
                            isAvailable = false;
                            break;
                        }
                    }

                    if (callback != null) {
                        callback.onAvailabilityChecked(isAvailable);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onAvailabilityChecked(true);
                    }
                });
    }

    public static void setupTimeSlotListener(String date, String serviceCategory, TimeSlotListener callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String normalizedCategory = normalizeCategory(serviceCategory);

        db.collection("bookings")
                .whereEqualTo("bookingDate", date)
                .whereEqualTo("serviceCategory", normalizedCategory)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        if (callback != null) callback.onTimeSlotUpdate(null);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<TimeSlotUpdate> updates = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String status = document.getString("status");
                            String bookingTime = document.getString("bookingTime");
                            String bookingId = document.getId();

                            if (bookingTime != null) {
                                boolean isAvailable = status == null ||
                                        status.equals("completed") || status.equals("cancelled");

                                updates.add(new TimeSlotUpdate(bookingTime, isAvailable, bookingId));
                            }
                        }
                        if (callback != null) callback.onTimeSlotUpdate(updates);
                    }
                });
    }

    private static String normalizeCategory(String serviceCategory) {
        if (serviceCategory == null || serviceCategory.isEmpty()) {
            return "general";
        }
        String category = serviceCategory.toLowerCase().trim();
        if (category.contains("lash")) return "lashes";
        else if (category.contains("hair")) return "hair";
        else if (category.contains("nail")) return "nails";
        else return "general";
    }

    public interface AvailabilityCheckCallback {
        void onAvailabilityChecked(List<TimeSlotItem> updatedTimeSlots);
    }

    public interface TimeSlotListener {
        void onTimeSlotUpdate(List<TimeSlotUpdate> updates);
    }

    public interface SingleAvailabilityCallback {
        void onAvailabilityChecked(boolean isAvailable);
    }

    public static class TimeSlotUpdate {
        public final String timeSlot;
        public final boolean isAvailable;
        public final String bookingId;

        public TimeSlotUpdate(String timeSlot, boolean isAvailable, String bookingId) {
            this.timeSlot = timeSlot;
            this.isAvailable = isAvailable;
            this.bookingId = bookingId;
        }
    }

    public static List<TimeSlotItem> createTimeSlotsForDate(String date) {
        List<TimeSlotItem> timeSlots = new ArrayList<>();

        String[] morningSlots = {"09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM"};
        String[] afternoonSlots = {"12:00 PM", "12:30 PM", "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM",
                "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM"};
        String[] eveningSlots = {"05:00 PM", "05:30 PM", "06:00 PM"};

        for (String slot : morningSlots) {
            timeSlots.add(new TimeSlotItem(slot, "Morning", true, date));
        }
        for (String slot : afternoonSlots) {
            timeSlots.add(new TimeSlotItem(slot, "Afternoon", true, date));
        }
        for (String slot : eveningSlots) {
            timeSlots.add(new TimeSlotItem(slot, "Evening", true, date));
        }

        return timeSlots;
    }

    @Override
    public String toString() {
        return "TimeSlotItem{" +
                "timeSlot='" + timeSlot + '\'' +
                ", grouping='" + grouping + '\'' +
                ", isAvailable=" + isAvailable +
                ", date='" + date + '\'' +
                ", bookingId='" + bookingId + '\'' +
                '}';
    }

    public static class CategoryServicesActivity {
    }
}