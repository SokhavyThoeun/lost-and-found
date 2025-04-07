package com.example.mylostandfoundapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.mylostandfoundapplication.models.LostItem;
import com.example.mylostandfoundapplication.adapters.LostItemAdapter;
import com.example.mylostandfoundapplication.R;
import com.example.mylostandfoundapplication.MyPostsActivity;
import com.example.mylostandfoundapplication.ProfileActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private RecyclerView recyclerView;
    private LostItemAdapter adapter;
    private List<LostItem> itemList;
    private List<LostItem> allItems;
    private FirebaseAuth auth;
    private DatabaseReference itemsRef;
    private TextInputEditText searchEditText;
    private MaterialButton searchButton;
    private TextView noResultsText;
    private boolean isSearching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        itemsRef = FirebaseDatabase.getInstance().getReference("items");

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemList = new ArrayList<>();
        allItems = new ArrayList<>();
        adapter = new LostItemAdapter();
        adapter.setItems(itemList);
        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(HomeActivity.this, ItemDetailsActivity.class);
            intent.putExtra("itemId", item.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Set up Search
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        noResultsText = findViewById(R.id.noResultsText);
        
        searchButton.setOnClickListener(v -> performSearch());
        
        // Add text change listener for real-time search
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    performSearch();
                } else if (isSearching) {
                    clearSearch();
                }
            }
        });

        // Set up Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(this);

        // Set up FAB
        FloatingActionButton fab = findViewById(R.id.fabAddItem);
        fab.setOnClickListener(v -> {
            if (auth.getCurrentUser() != null) {
                Intent intent = new Intent(this, PostItemActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.must_login_to_post, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
            }
        });

        // Set up RecyclerView scroll listener to coordinate with FAB behavior
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fab.isShown()) {
                    fab.hide();
                } else if (dy < 0 && !fab.isShown()) {
                    fab.show();
                }
            }
        });

        // Load items
        loadItems();
    }

    private void loadItems() {
        itemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allItems.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LostItem item = snapshot.getValue(LostItem.class);
                    if (item != null) {
                        item.setId(snapshot.getKey());
                        allItems.add(item);
                    }
                }
                
                // If not searching, update the displayed items
                if (!isSearching) {
                    itemList.clear();
                    itemList.addAll(allItems);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().toLowerCase().trim();
        
        if (query.isEmpty()) {
            clearSearch();
            return;
        }
        
        isSearching = true;
        itemList.clear();
        
        for (LostItem item : allItems) {
            if (item.getTitle() != null && item.getTitle().toLowerCase().contains(query) ||
                item.getDescription() != null && item.getDescription().toLowerCase().contains(query) ||
                item.getLocation() != null && item.getLocation().toLowerCase().contains(query)) {
                itemList.add(item);
            }
        }
        
        adapter.notifyDataSetChanged();
        
        // Show/hide no results text
        if (itemList.isEmpty()) {
            noResultsText.setVisibility(View.VISIBLE);
        } else {
            noResultsText.setVisibility(View.GONE);
        }
    }
    
    private void clearSearch() {
        isSearching = false;
        searchEditText.setText("");
        itemList.clear();
        itemList.addAll(allItems);
        adapter.notifyDataSetChanged();
        noResultsText.setVisibility(View.GONE);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.navigation_home) {
            return true;
        } else if (itemId == R.id.navigation_my_posts) {
            startActivity(new Intent(this, MyPostsActivity.class));
            return true;
        } else if (itemId == R.id.navigation_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        
        return false;
    }

    private void signOut() {
        auth.signOut();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
} 