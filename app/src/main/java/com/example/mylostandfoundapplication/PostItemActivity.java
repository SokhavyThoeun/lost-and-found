package com.example.mylostandfoundapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.mylostandfoundapplication.models.LostItem;
import com.example.mylostandfoundapplication.utils.ImageUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PostItemActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText titleEditText, descriptionEditText, locationEditText, contactInfoEditText, dateEditText;
    private ImageView itemImageView;
    private String imageBase64;
    private FirebaseAuth auth;
    private DatabaseReference itemsRef;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_item);

        // Set up toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.add_new_item);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        itemsRef = FirebaseDatabase.getInstance().getReference("items");

        // Initialize calendar
        calendar = Calendar.getInstance();

        // Initialize views
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        locationEditText = findViewById(R.id.locationEditText);
        contactInfoEditText = findViewById(R.id.contactInfoEditText);
        dateEditText = findViewById(R.id.dateEditText);
        itemImageView = findViewById(R.id.itemImageView);
        Button selectImageButton = findViewById(R.id.selectImageButton);
        Button postButton = findViewById(R.id.postButton);

        // Set up date picker
        dateEditText.setOnClickListener(v -> showDatePicker());
        
        // Set default date to today
        updateDateInView();

        // Set up click listeners
        selectImageButton.setOnClickListener(v -> openImagePicker());
        postButton.setOnClickListener(v -> postItem());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                itemImageView.setImageBitmap(bitmap);
                imageBase64 = bitmapToBase64(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateInView();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateInView() {
        String format = "MMM dd, yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        dateEditText.setText(dateFormat.format(calendar.getTime()));
    }

    private void postItem() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String contactInfo = contactInfoEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        String userId = auth.getCurrentUser().getUid();

        // Validate title
        if (title.isEmpty()) {
            titleEditText.setError(getString(R.string.error_title_required));
            titleEditText.requestFocus();
            return;
        }
        if (title.length() < 3) {
            titleEditText.setError(getString(R.string.error_title_length));
            titleEditText.requestFocus();
            return;
        }

        // Validate description
        if (description.isEmpty()) {
            descriptionEditText.setError(getString(R.string.error_description_required));
            descriptionEditText.requestFocus();
            return;
        }
        if (description.length() < 10) {
            descriptionEditText.setError(getString(R.string.error_description_length));
            descriptionEditText.requestFocus();
            return;
        }

        // Validate location
        if (location.isEmpty()) {
            locationEditText.setError(getString(R.string.error_location_required));
            locationEditText.requestFocus();
            return;
        }

        // Validate contact info
        if (contactInfo.isEmpty()) {
            contactInfoEditText.setError(getString(R.string.error_contact_required));
            contactInfoEditText.requestFocus();
            return;
        }
        if (!isValidPhoneOrEmail(contactInfo)) {
            contactInfoEditText.setError(getString(R.string.error_contact_invalid));
            contactInfoEditText.requestFocus();
            return;
        }

        // Validate image
        if (imageBase64 == null) {
            Toast.makeText(this, getString(R.string.error_image_required), Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable post button and show progress
        Button postButton = findViewById(R.id.postButton);
        postButton.setEnabled(false);
        postButton.setText(R.string.posting);

        LostItem item = new LostItem(title, description, location, date, userId, imageBase64, contactInfo);
        String itemId = itemsRef.push().getKey();
        if (itemId != null) {
            itemsRef.child(itemId).setValue(item)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PostItemActivity.this, getString(R.string.msg_post_success), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    String errorMessage = getString(R.string.msg_post_failed, e.getMessage());
                    Toast.makeText(PostItemActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    postButton.setEnabled(true);
                    postButton.setText(R.string.post_item);
                });
        }
    }

    private boolean isValidPhoneOrEmail(String input) {
        // Simple email validation
        if (input.contains("@") && input.contains(".")) {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches();
        }
        // Simple phone validation (at least 10 digits)
        return input.replaceAll("[^0-9]", "").length() >= 10;
    }
} 