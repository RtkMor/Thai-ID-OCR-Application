package com.example.thaiocrscanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CrudActivity extends AppCompatActivity implements CrudAdapter.OnEditButtonClickListener, CrudAdapter.OnDeleteButtonClickListener {

    // Search Options
    private LinearLayout searchOptions;
    private boolean searchOptionsVisible = false;
    private boolean name = true;
    private boolean lastName = false;
    private boolean idNumber = false;
    private TextView nameButton, lastNameButton, idNumberButton;
    private ImageView dropBtn;
    private EditText searchEditText;

    // Sort/Filter Options
    private TextView randomButton, alphabeticalButton;
    private Boolean random = true;
    private Boolean alphabetical = false;
    private RecyclerView recyclerView;
    private CrudAdapter adapter;
    private List<DataSnapshot> crudSnapshots;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;
    private List<HashMap<String, String>> crudList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crud);
        Objects.requireNonNull(getSupportActionBar()).hide();

        // UI elements
        searchEditText = findViewById(R.id.searchEditText);
        searchOptions = findViewById(R.id.searchOptions);
        searchOptions.setVisibility(View.GONE);
        nameButton = findViewById(R.id.nameButton);
        lastNameButton = findViewById(R.id.lastNameButton);
        idNumberButton = findViewById(R.id.idNumberButton);
        dropBtn = findViewById(R.id.dropBtn);
        recyclerView = findViewById(R.id.recyclerViewCrud);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        randomButton = findViewById(R.id.randomButton);
        alphabeticalButton = findViewById(R.id.alphabeticalButton);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE); // Set ProgressBar initially visible

        // search bar implementation to hide and show the options + keyboard
        setDropBtnEditTextClickListener();
        setNameButtonClickListener();
        setLastNameButtonClickListener();
        setIdNumberButtonClickListener();
        View rootView = findViewById(R.id.rootLayout);
        setRootViewTouchListener(rootView);
        setAlphabeticalButtonClickListener();
        setRandomButtonClickListener();

        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Database");

        // Query to retrieve data (change the query as needed)
        Query query = databaseReference.orderByKey();

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                crudSnapshots = new ArrayList<>(); // Initialize the list
                crudList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    crudSnapshots.add(snapshot); // Store the DataSnapshot to edit them
                    HashMap<String, String> dataMap = new HashMap<>();
                    dataMap.put("name", snapshot.child("Name").getValue(String.class));
                    dataMap.put("lastName", snapshot.child("Last Name").getValue(String.class));
                    dataMap.put("identificationNumber", snapshot.child("Identification Number").getValue(String.class));
                    dataMap.put("dob", snapshot.child("Date Of Birth").getValue(String.class));
                    dataMap.put("issue", snapshot.child("Date Of Issue").getValue(String.class));
                    dataMap.put("expiry", snapshot.child("Date Of Expiry").getValue(String.class));
                    crudList.add(dataMap);
                }
                adapter = new CrudAdapter(crudList, CrudActivity.this, CrudActivity.this);
                recyclerView.setAdapter(adapter);

                progressBar.setVisibility(View.GONE); // Hide loading screen after data is loaded
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE); // Hide loading screen in case of database error
                // Handle database error
            }
        });


    }

    @Override
    public void onEditButtonClick(HashMap<String, String> crudItem) {
        // Handle the edit button click
        Intent intent = new Intent(CrudActivity.this, EditActivity.class);
        // Pass the data to EditActivity using extras in the intent
        intent.putExtra("name", crudItem.get("name"));
        intent.putExtra("lastName", crudItem.get("lastName"));
        intent.putExtra("identificationNumber", crudItem.get("identificationNumber"));
        intent.putExtra("dob", crudItem.get("dob"));
        intent.putExtra("issue", crudItem.get("issue"));
        intent.putExtra("expiry", crudItem.get("expiry"));
        startActivity(intent);
    }
    @Override
    public void onDeleteButtonClick(int position) {
        // Handle the delete button click
        if (position >= 0 && position < crudSnapshots.size()) {
            DataSnapshot itemToDeleteSnapshot = crudSnapshots.get(position);
            // Get the Firebase key of the item to delete
            String itemId = itemToDeleteSnapshot.getKey();

            if (itemId != null) {
                DatabaseReference nodeToDelete = FirebaseDatabase.getInstance().getReference().child("Database").child(itemId);

                // Remove the node from the database
                nodeToDelete.removeValue()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Node deleted successfully
                                // You can also remove the item from the local list if needed
                                crudSnapshots.remove(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, crudSnapshots.size());
                                Toast.makeText(CrudActivity.this, "Data deleted successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                // Handle the failure to delete the node
                                Toast.makeText(CrudActivity.this, "Failed to delete the node!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    // Search Bar Related Implementation
    @SuppressLint("ClickableViewAccessibility")
    private void setRootViewTouchListener(View rootView) {
        rootView.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });
    }
    private void setDropBtnEditTextClickListener() {
        dropBtn.setOnClickListener(v -> {
            if (!searchOptionsVisible) {
                showSearchOptions();
                dropBtn.setImageResource(R.drawable.baseline_arrow_drop_up_white_24);
            } else {
                hideSearchOptions();
                dropBtn.setImageResource(R.drawable.baseline_arrow_drop_down_white_24);
            }
        });

        // Add TextWatcher to perform search as the text changes
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Perform search based on the current option (name, lastName, idNumber)
                String query = charSequence.toString().trim();
                loadData(query);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
    private void loadData(String query) {
        progressBar.setVisibility(View.VISIBLE);

        // Query to retrieve data based on the selected option
        Query dataQuery;
        if (name) {
            dataQuery = databaseReference.orderByChild("Name").startAt(query).endAt(query + "\uf8ff");
        } else if (lastName) {
            dataQuery = databaseReference.orderByChild("Last Name").startAt(query).endAt(query + "\uf8ff");
        } else {
            dataQuery = databaseReference.orderByChild("Identification Number").startAt(query).endAt(query + "\uf8ff");
        }

        dataQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                crudSnapshots = new ArrayList<>(); // Initialize the list
                crudList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    crudSnapshots.add(snapshot);
                    HashMap<String, String> dataMap = new HashMap<>();
                    dataMap.put("name", snapshot.child("Name").getValue(String.class));
                    dataMap.put("lastName", snapshot.child("Last Name").getValue(String.class));
                    dataMap.put("identificationNumber", snapshot.child("Identification Number").getValue(String.class));
                    dataMap.put("dob", snapshot.child("Date Of Birth").getValue(String.class));
                    dataMap.put("issue", snapshot.child("Date Of Issue").getValue(String.class));
                    dataMap.put("expiry", snapshot.child("Date Of Expiry").getValue(String.class));
                    crudList.add(dataMap);
                }

                adapter = new CrudAdapter(crudList, CrudActivity.this, CrudActivity.this);
                recyclerView.setAdapter(adapter);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                // Handle database error
            }
        });
    }

    private void setNameButtonClickListener() {
        nameButton.setOnClickListener(v -> {
            name = true;
            lastName = false;
            idNumber = false;
            updateSearchOptionsVisibility();
        });
    }
    private void setLastNameButtonClickListener() {
        lastNameButton.setOnClickListener(v -> {
            name = false;
            lastName = true;
            idNumber = false;
            updateSearchOptionsVisibility();
        });
    }
    private void setIdNumberButtonClickListener() {
        idNumberButton.setOnClickListener(v -> {
            name = false;
            lastName = false;
            idNumber = true;
            updateSearchOptionsVisibility();
        });
    }
    private void showSearchOptions() {
        searchOptions.setVisibility(View.VISIBLE);
        searchOptionsVisible = true;
    }
    private void hideSearchOptions() {
        searchOptions.setVisibility(View.GONE);
        searchOptionsVisible = false;
    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }
    private void updateSearchOptionsVisibility() {
        updateButtonUI(name, nameButton);
        updateButtonUI(lastName, lastNameButton);
        updateButtonUI(idNumber, idNumberButton);
    }
    private void updateButtonUI(boolean selected, TextView button) {
        int backgroundColor;
        int textColor;

        if (selected) {
            backgroundColor = R.color.black;
            textColor = R.color.white;
        } else {
            backgroundColor = R.color.white;
            textColor = R.color.black;
        }

        button.setBackgroundResource(backgroundColor);
        button.setTextColor(ContextCompat.getColor(this, textColor));
    }
    private void setAlphabeticalButtonClickListener() {
        alphabeticalButton.setOnClickListener(v -> {
            alphabetical = true;
            random = false;
            updateSortOptionsVisibility();
        });
    }
    private void setRandomButtonClickListener() {
        randomButton.setOnClickListener(v -> {
            alphabetical = false;
            random = true;
            updateSortOptionsVisibility();
        });
    }
    private void updateSortOptionsVisibility() {
        updateButtonUI(alphabetical, alphabeticalButton);
        updateButtonUI(random, randomButton);

        // Reorder the list based on the selected sorting criteria
        if (alphabetical) {
            sortAlphabetically();
        } else if (random) {
            sortRandomly();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void sortAlphabetically() {
        Collections.sort(crudList, (item1, item2) -> {
            String name1 = item1.get("name");
            String name2 = item2.get("name");
            if(!Objects.equals(name1, name2)) {
                assert name2 != null;
                assert name1 != null;
                return name1.compareTo(name2);
            }
            else
                return Objects.requireNonNull(item1.get("lastName")).compareTo(Objects.requireNonNull(item2.get("lastName")));
        });
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void sortRandomly() {
        Collections.shuffle(crudList);
        adapter.notifyDataSetChanged();
    }

    public void searchBtnClicked(View view) {
        String str = searchEditText.getText().toString().trim();
        loadData(str);
    }
}
