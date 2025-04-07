package com.example.mylostandfoundapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.mylostandfoundapplication.utils.ImageUtils;
import com.example.mylostandfoundapplication.models.LostItem;

public class ItemDetailsActivity extends AppCompatActivity {
    private ImageView itemImage;
    private TextView itemTitle, itemDescription, itemLocation, itemDate, itemContactInfo;
    private MaterialButton contactButton;
    private DatabaseReference itemsRef;
    private String itemId;
    private LostItem currentItem;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Set up toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize views
        itemImage = findViewById(R.id.itemImage);
        itemTitle = findViewById(R.id.itemTitle);
        itemDescription = findViewById(R.id.itemDescription);
        itemLocation = findViewById(R.id.itemLocation);
        itemDate = findViewById(R.id.itemDate);
        itemContactInfo = findViewById(R.id.itemContactInfo);
        contactButton = findViewById(R.id.contactButton);

        // Get item ID from intent
        itemId = getIntent().getStringExtra("itemId");
        if (itemId == null) {
            finish();
            return;
        }

        // Initialize Firebase
        itemsRef = FirebaseDatabase.getInstance().getReference("items").child(itemId);
        loadItemDetails();

        // Set up button click listeners
        setupContactButton();
    }

    private void loadItemDetails() {
        itemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentItem = dataSnapshot.getValue(LostItem.class);
                if (currentItem != null) {
                    itemTitle.setText(currentItem.getTitle());
                    itemDescription.setText(currentItem.getDescription());
                    itemLocation.setText(currentItem.getLocation());
                    itemDate.setText(currentItem.getDate());
                    itemContactInfo.setText(currentItem.getContactInfo());

                    if (currentItem.getImageBase64() != null && !currentItem.getImageBase64().isEmpty()) {
                        itemImage.setImageBitmap(ImageUtils.base64ToBitmap(currentItem.getImageBase64()));
                    } else {
                        itemImage.setImageResource(R.drawable.ic_image_placeholder);
                    }

                    // Update UI based on item status
                    updateUIForItemStatus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ItemDetailsActivity.this, R.string.error_loading_item, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIForItemStatus() {
        if (currentItem != null) {
            boolean isOwner = auth.getCurrentUser() != null && 
                            auth.getCurrentUser().getUid().equals(currentItem.getUserId());

            // Show/hide buttons based on ownership
            if (isOwner) {
                contactButton.setVisibility(View.GONE);
            } else {
                contactButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupContactButton() {
        contactButton.setOnClickListener(v -> {
            if (currentItem != null && currentItem.getContactInfo() != null) {
                showContactOptions();
            }
        });
    }

    private void showContactOptions() {
        String[] options = {
            getString(R.string.contact_via_sms),
            getString(R.string.contact_via_call)
        };

        new AlertDialog.Builder(this)
            .setTitle(R.string.choose_contact_method)
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // SMS
                        sendSMS();
                        break;
                    case 1: // Call
                        makePhoneCall();
                        break;
                }
            })
            .show();
    }

    private void sendSMS() {
        String contactInfo = currentItem.getContactInfo();
        if (contactInfo != null && !contactInfo.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("sms:" + contactInfo));
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.no_phone_number, Toast.LENGTH_SHORT).show();
        }
    }

    private void makePhoneCall() {
        String contactInfo = currentItem.getContactInfo();
        if (contactInfo != null && !contactInfo.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + contactInfo));
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.no_phone_number, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_details, menu);
        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        if (deleteItem != null && currentItem != null && auth.getCurrentUser() != null) {
            boolean isOwner = auth.getCurrentUser().getUid().equals(currentItem.getUserId());
            deleteItem.setVisible(isOwner);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            if (currentItem != null && auth.getCurrentUser() != null && 
                auth.getCurrentUser().getUid().equals(currentItem.getUserId())) {
                showDeleteConfirmationDialog();
            } else {
                Toast.makeText(this, R.string.error_not_owner, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.delete_confirmation_title)
            .setMessage(R.string.delete_confirmation_message)
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton(R.string.yes, (dialog, which) -> deleteItem())
            .setNegativeButton(R.string.no, null)
            .show();
    }

    private void deleteItem() {
        // Show progress dialog while deleting
        AlertDialog progressDialog = new AlertDialog.Builder(this)
            .setMessage(R.string.deleting_item)
            .setCancelable(false)
            .show();

        itemsRef.removeValue()
            .addOnSuccessListener(aVoid -> {
                progressDialog.dismiss();
                Toast.makeText(this, R.string.item_deleted_success, Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, R.string.error_deleting_item, Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 