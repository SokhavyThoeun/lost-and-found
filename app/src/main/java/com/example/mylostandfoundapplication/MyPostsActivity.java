package com.example.mylostandfoundapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.mylostandfoundapplication.models.LostItem;
import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private RecyclerView recyclerView;
    private LostItemAdapter adapter;
    private List<LostItem> itemList;
    private FirebaseAuth auth;
    private DatabaseReference itemsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_my_posts);
        }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        itemsRef = FirebaseDatabase.getInstance().getReference("items");

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemList = new ArrayList<>();
        adapter = new LostItemAdapter(this, itemList, item -> {
            Intent intent = new Intent(MyPostsActivity.this, ItemDetailsActivity.class);
            intent.putExtra("itemId", item.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Set up Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(this);
        bottomNav.setSelectedItemId(R.id.navigation_my_posts);

        // Set up FAB
        FloatingActionButton fab = findViewById(R.id.fabAddItem);
        fab.setOnClickListener(v -> startActivity(new Intent(this, PostItemActivity.class)));

        // Load user's posts
        loadMyPosts();
    }

    private void loadMyPosts() {
        String userId = auth.getCurrentUser().getUid();
        Query query = itemsRef.orderByChild("userId").equalTo(userId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LostItem item = snapshot.getValue(LostItem.class);
                    if (item != null) {
                        item.setId(snapshot.getKey());
                        itemList.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.navigation_home) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return true;
        } else if (itemId == R.id.navigation_my_posts) {
            return true;
        } else if (itemId == R.id.navigation_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            return true;
        }
        
        return false;
    }
} 