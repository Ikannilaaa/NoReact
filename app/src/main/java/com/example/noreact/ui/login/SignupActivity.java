package com.example.noreact.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.noreact.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText signupName, signupEmail, signupUsername, signupPassword;
    private Button signupButton;
    private TextView loginRedirectText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        signupButton.setOnClickListener(v -> registerUser());
        loginRedirectText.setOnClickListener(v -> redirectToLogin());
    }

    private void registerUser() {
        String name = signupName.getText().toString().trim();
        String email = signupEmail.getText().toString().trim();
        String username = signupUsername.getText().toString().trim();
        String password = signupPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            signupName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            signupEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(username)) {
            signupUsername.setError("Username is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            signupPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            signupPassword.setError("Password must be at least 6 characters");
            return;
        }

        signupButton.setEnabled(false);
        signupButton.setText("Creating Account...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save user data to Firestore
                            saveUserData(user.getUid(), name, email, username);
                            // Send verification email
                            sendEmailVerification(user);
                        }
                    } else {
                        signupButton.setEnabled(true);
                        signupButton.setText("Sign Up");
                        Toast.makeText(SignupActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(String userId, String name, String email, String username) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("username", username);
        user.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    // Data saved successfully
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignupActivity.this,
                            "Failed to save user data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    signupButton.setEnabled(true);
                    signupButton.setText("Sign Up");

                    if (task.isSuccessful()) {
                        Toast.makeText(SignupActivity.this,
                                "Registration successful! Verification email sent. Please check your email.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SignupActivity.this,
                                "Registration successful but failed to send verification email.",
                                Toast.LENGTH_SHORT).show();
                    }
                    redirectToLogin();
                });
    }

    private void redirectToLogin() {
        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        finish();
    }
}