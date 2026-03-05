package com.example.bookingapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private ImageView ivAvatar;
    private TextView tvProfileEmail;

    private Button btnEditProfile;
    private Button btnMyBookings;
    private Button btnChangePassword;
    private Button btnLogout;
    private ImageButton backButton;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();

        // Bind views (no more tvProfileName)
        backButton        = findViewById(R.id.btn_back);
        ivAvatar          = findViewById(R.id.iv_profile_avatar);
        tvProfileEmail    = findViewById(R.id.tv_profile_email);

        btnEditProfile    = findViewById(R.id.btn_edit_profile);
        btnMyBookings     = findViewById(R.id.btn_my_bookings);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnLogout         = findViewById(R.id.btn_logout);

        loadUserDetails();

        // Back button
        backButton.setOnClickListener(v -> finish());

        // EDIT PROFILE
        btnEditProfile.setOnClickListener(v -> {
            Intent i = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(i);
        });

        // MY BOOKINGS
        btnMyBookings.setOnClickListener(v -> {
            Intent i = new Intent(ProfileActivity.this, AppointmentsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });

        // CHANGE PASSWORD
        btnChangePassword.setOnClickListener(v -> {
            Intent i = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(i);
        });

        // LOGOUT
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(ProfileActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(ProfileActivity.this, RegisterActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        // ---- BOTTOM NAVIGATION ----
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_profile);

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    Intent i = new Intent(ProfileActivity.this, CategoryActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(i);
                    finish();
                    return true;
                }

                if (id == R.id.nav_appointments) {
                    Intent i = new Intent(ProfileActivity.this, AppointmentsActivity.class);
                    startActivity(i);
                    finish();
                    return true;
                }

                return id == R.id.nav_profile;
            });
        }
    }

    private void loadUserDetails() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            tvProfileEmail.setText("No email found");
            ivAvatar.setImageResource(R.drawable.ic_person_large);
            return;
        }

        // EMAIL ONLY
        tvProfileEmail.setText(
                user.getEmail() != null ? user.getEmail() : "No email available"
        );

        // Avatar
        if (user.getPhotoUrl() != null) {
            try {
                ivAvatar.setImageURI(user.getPhotoUrl());
            } catch (Exception e) {
                ivAvatar.setImageResource(R.drawable.ic_person_large);
            }
        } else {
            ivAvatar.setImageResource(R.drawable.ic_person_large);
        }
    }
}
