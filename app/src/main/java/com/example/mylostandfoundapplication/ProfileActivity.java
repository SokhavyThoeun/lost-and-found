package com.example.mylostandfoundapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.mylostandfoundapplication.databinding.ActivityProfileBinding;
import com.example.mylostandfoundapplication.utils.ImageUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.IOException;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private FirebaseAuth mAuth;
    private String userId;
    private Uri selectedImageUri;
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    binding.ivProfilePicture.setImageURI(selectedImageUri);
                    uploadProfilePicture();
                }
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        loadUserProfile();

        binding.btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                updateProfile();
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            mAuth.signOut();
            startActivity(new Intent(ProfileActivity.this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });

        binding.btnChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
    }

    private void uploadProfilePicture() {
        if (selectedImageUri == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        try {
            // Convert URI to Bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            
            // Compress the bitmap to reduce size
            Bitmap compressedBitmap = ImageUtils.compressBitmap(bitmap, 800, 800);
            
            // Convert bitmap to base64
            String base64Image = ImageUtils.bitmapToBase64(compressedBitmap);

            // Update user profile with base64 image
            FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("profilePictureBase64")
                .setValue(base64Image)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, 
                        "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, 
                        "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                });
        } catch (IOException e) {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void loadUserProfile() {
        FirebaseDatabase.getInstance().getReference("users")
            .child(userId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        binding.etFirstName.setText(user.getFirstName());
                        binding.etLastName.setText(user.getLastName());
                        binding.etEmail.setText(user.getEmail());
                        
                        // Load profile picture from base64
                        if (user.getProfilePictureBase64() != null && !user.getProfilePictureBase64().isEmpty()) {
                            Bitmap bitmap = ImageUtils.base64ToBitmap(user.getProfilePictureBase64());
                            binding.ivProfilePicture.setImageBitmap(bitmap);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ProfileActivity.this, 
                        "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private boolean validateInputs() {
        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateProfile() {
        binding.progressBar.setVisibility(View.VISIBLE);

        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();

        FirebaseDatabase.getInstance().getReference("users")
            .child(userId)
            .child("firstName")
            .setValue(firstName)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseDatabase.getInstance().getReference("users")
                        .child(userId)
                        .child("lastName")
                        .setValue(lastName)
                        .addOnCompleteListener(task2 -> {
                            binding.progressBar.setVisibility(View.GONE);
                            if (task2.isSuccessful()) {
                                Toast.makeText(ProfileActivity.this, 
                                    "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProfileActivity.this, 
                                    "Failed to update profile", Toast.LENGTH_SHORT).show();
                            }
                        });
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, 
                        "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 