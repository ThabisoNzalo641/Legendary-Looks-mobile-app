package com.example.bookingapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private final List<Appointment> appointmentList;

    public AppointmentAdapter(List<Appointment> appointmentList) {
        this.appointmentList = appointmentList;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment_card, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);

        holder.serviceName.setText(appointment.getServiceName());
        holder.dateTime.setText(appointment.getDateTime());
        holder.status.setText(appointment.getFormattedStatus());

        // Set status color based on status
        setStatusColor(holder.status, appointment.getStatus());

        // REMOVED the price section that was causing the crash
        // If you want to show price, make sure the TextView exists in your XML
    }

    private void setStatusColor(TextView statusView, String status) {
        if (status == null) return;

        switch (status.toLowerCase()) {
            case "pending":
                statusView.setTextColor(Color.parseColor("#FF9800")); // Orange
                break;
            case "confirmed":
                statusView.setTextColor(Color.parseColor("#2196F3")); // Blue
                break;
            case "completed":
                statusView.setTextColor(Color.parseColor("#4CAF50")); // Green
                break;
            case "cancelled":
                statusView.setTextColor(Color.parseColor("#F44336")); // Red
                break;
            default:
                statusView.setTextColor(Color.parseColor("#666666")); // Gray
        }
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName;
        TextView dateTime;
        TextView status;
        // REMOVED: TextView price; // This was causing the crash

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.tv_service_name);
            dateTime = itemView.findViewById(R.id.tv_appointment_datetime);
            status = itemView.findViewById(R.id.tv_appointment_status);
            // REMOVED: price = itemView.findViewById(R.id.tv_appointment_price); // This doesn't exist in XML
        }
    }

    // Method to update data
    public void updateData(List<Appointment> newAppointments) {
        appointmentList.clear();
        appointmentList.addAll(newAppointments);
        notifyDataSetChanged();
    }
}