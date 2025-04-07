package com.example.mylostandfoundapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mylostandfoundapplication.databinding.ActivitySignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnSignUp.setOnClickListener(v -> {
            String firstName = binding.etFirstName.getText().toString().trim();
            String lastName = binding.etLastName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

            if (validateInputs(firstName, lastName, email, password, confirmPassword)) {
                createAccount(firstName, lastName, email, password);
            }
        });
    }

    private boolean validateInputs(String firstName, String lastName, String email, 
                                 String password, String confirmPassword) {
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || 
            password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void createAccount(String firstName, String lastName, String email, String password) {
        binding.progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Save user details to database
                    String userId = mAuth.getCurrentUser().getUid();
                    User user = new User(userId, firstName, lastName, email);
                    
                    FirebaseDatabase.getInstance().getReference("users")
                        .child(userId)
                        .setValue(user)
                        .addOnCompleteListener(dbTask -> {
                            binding.progressBar.setVisibility(View.GONE);
                            if (dbTask.isSuccessful()) {
                                startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                                finish();
                            } else {
                                Toast.makeText(SignUpActivity.this, 
                                    "Failed to save user data", Toast.LENGTH_SHORT).show();
                            }
                        });
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignUpActivity.this, 
                        "Failed to create account", Toast.LENGTH_SHORT).show();
                }
            });
    }
} 