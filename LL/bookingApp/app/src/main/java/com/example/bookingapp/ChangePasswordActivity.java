package com.example.bookingapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private static final String TAG = "ChangePasswordActivity";

    private EditText etOldPassword, etNewPassword, etConfirmPassword;
    private Button btnChange, btnCancel;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        auth = FirebaseAuth.getInstance();

        etOldPassword     = findViewById(R.id.et_old_password);
        etNewPassword     = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnChange         = findViewById(R.id.btn_change);
        btnCancel         = findViewById(R.id.btn_cancel);

        btnCancel.setOnClickListener(v -> finish());

        btnChange.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String oldPass = etOldPassword.getText().toString();
        String newPass = etNewPassword.getText().toString();
        String confirm = etConfirmPassword.getText().toString();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        // RE-AUTH using old password
        AuthCredential credential =
                EmailAuthProvider.getCredential(user.getEmail(), oldPass);

        user.reauthenticate(credential).addOnSuccessListener(aVoid -> {
            // After successful re-auth → update password
            user.updatePassword(newPass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ChangePasswordActivity.this, "Password changed", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Failed to change password", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "updatePassword failed", task.getException());
                }
            });

        }).addOnFailureListener(e -> {
            Toast.makeText(ChangePasswordActivity.this, "Old password incorrect", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Reauth failed", e);
        });
    }
}
