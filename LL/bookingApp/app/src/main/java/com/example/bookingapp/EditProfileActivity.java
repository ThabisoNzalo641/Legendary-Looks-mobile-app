package com.example.bookingapp;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";

    private EditText etEmail;
    private EditText etPhotoUrl;
    private ImageView ivPreview;
    private Button btnSave;
    private Button btnCancel;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.et_email);
        etPhotoUrl = findViewById(R.id.et_photo_url);
        ivPreview = findViewById(R.id.iv_photo_preview);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        // Load current user info
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            if (user.getEmail() != null) etEmail.setText(user.getEmail());
            if (user.getPhotoUrl() != null) {
                etPhotoUrl.setText(user.getPhotoUrl().toString());
                Glide.with(this).load(user.getPhotoUrl()).into(ivPreview);
            } else {
                ivPreview.setImageResource(R.drawable.ic_person_large);
            }
        }

        // Preview photo URL when focus leaves field
        etPhotoUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) loadPreviewFromUrl();
        });

        btnSave.setOnClickListener(v -> {
            String newEmail = etEmail.getText().toString().trim();
            String photo = etPhotoUrl.getText().toString().trim();

            if (newEmail.isEmpty()) {
                etEmail.setError("Please enter an email");
                etEmail.requestFocus();
                return;
            }

            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "No user signed in", Toast.LENGTH_SHORT).show();
                return;
            }

            // If email changed, try updateEmail first (may require recent auth)
            String currentEmail = currentUser.getEmail() != null ? currentUser.getEmail() : "";
            boolean emailChanged = !newEmail.equals(currentEmail);

            if (emailChanged) {
                currentUser.updateEmail(newEmail)
                        .addOnCompleteListener(emailTask -> {
                            if (emailTask.isSuccessful()) {
                                // Email updated — now update photo (if provided)
                                updatePhotoIfNeeded(currentUser, photo);
                            } else {
                                // Likely needs re-authentication
                                Log.w(TAG, "Failed to update email", emailTask.getException());
                                Toast.makeText(EditProfileActivity.this,
                                        "Email update failed. You may need to re-login and try again.",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                // Email unchanged — just update photo if needed
                updatePhotoIfNeeded(currentUser, photo);
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void updatePhotoIfNeeded(FirebaseUser user, String photo) {
        if (photo.isEmpty()) {
            // If user cleared photo field and they had a photo, clear it:
            UserProfileChangeRequest clearPhotoReq = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(null)
                    .build();

            user.updateProfile(clearPhotoReq)
                    .addOnCompleteListener(photoTask -> {
                        if (photoTask.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Log.w(TAG, "Failed to clear photo", photoTask.getException());
                            Toast.makeText(EditProfileActivity.this, "Profile updated but photo update failed", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });

            return;
        }

        // Try to parse photo URI and set it on the profile
        Uri photoUri = null;
        try {
            photoUri = Uri.parse(photo);
        } catch (Exception e) {
            Log.w(TAG, "Invalid photo URI: " + photo, e);
        }

        UserProfileChangeRequest.Builder reqBuilder = new UserProfileChangeRequest.Builder();
        if (photoUri != null) reqBuilder.setPhotoUri(photoUri);

        UserProfileChangeRequest req = reqBuilder.build();

        user.updateProfile(req)
                .addOnCompleteListener(profileTask -> {
                    if (profileTask.isSuccessful()) {
                        Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Log.e(TAG, "Profile update failed", profileTask.getException());
                        Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPreviewFromUrl() {
        String url = etPhotoUrl.getText().toString().trim();
        if (url.isEmpty()) {
            ivPreview.setImageResource(R.drawable.ic_person_large);
            return;
        }
        try {
            Uri uri = Uri.parse(url);
            Glide.with(this).load(uri).into(ivPreview);
        } catch (Exception e) {
            ivPreview.setImageResource(R.drawable.ic_person_large);
        }
    }
}
