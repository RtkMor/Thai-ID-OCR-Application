package com.example.thaiocrscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class EditActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Objects.requireNonNull(getSupportActionBar()).hide();

        EditText nameEt = findViewById(R.id.nameEt);
        EditText lastNameEt = findViewById(R.id.lastNameEt);
        EditText idNumberEt = findViewById(R.id.idNumberEt);
        EditText dobEt = findViewById(R.id.dobEt);
        EditText issueEt = findViewById(R.id.issueEt);
        EditText expiryEt = findViewById(R.id.expiryEt);
        TextView saveBtn = findViewById(R.id.saveBtn);

        // Retrieve data from intent extras or set default values
        Intent intent = getIntent();
        String name = intent != null ? intent.getStringExtra("name") : "ABC";
        String lastName = intent != null ? intent.getStringExtra("lastName") : "XYZ";
        String idNumber = intent != null ? intent.getStringExtra("identificationNumber") : "1xx2";
        String dob = intent != null ? intent.getStringExtra("dob") : "dd Jun. yyyy";
        String issue = intent != null ? intent.getStringExtra("issue") : "dd Jul. yyyy";
        String expiry = intent != null ? intent.getStringExtra("expiry") : "dd Aug. yyyy";

        // Set retrieved data to EditText fields
        nameEt.setText(name);
        lastNameEt.setText(lastName);
        idNumberEt.setText(idNumber);
        dobEt.setText(dob);
        issueEt.setText(issue);
        expiryEt.setText(expiry);

        // Handle save button click
        saveBtn.setOnClickListener(v -> {
            // Check if any of the EditText fields is empty
            if (TextUtils.isEmpty(nameEt.getText()) || TextUtils.isEmpty(lastNameEt.getText())
                    || TextUtils.isEmpty(idNumberEt.getText()) || TextUtils.isEmpty(dobEt.getText())
                    || TextUtils.isEmpty(issueEt.getText()) || TextUtils.isEmpty(expiryEt.getText())) {
                // Displaying a message that some fields are empty
                Toast.makeText(EditActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                // All fields are filled, save the data to Firebase
                saveDataToFirebase(
                        nameEt.getText().toString(),
                        lastNameEt.getText().toString(),
                        idNumberEt.getText().toString(),
                        dobEt.getText().toString(),
                        issueEt.getText().toString(),
                        expiryEt.getText().toString()
                );
            }
        });

        // Set a touch listener to hide the keyboard when the user clicks on any empty area
        findViewById(R.id.editRootLayout).setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false; // Return false to allow other touch events to be handled
        });

        // Handle back button click
        findViewById(R.id.backBtnEdit).setOnClickListener(v -> {
            finish();
        });
    }

    private void saveDataToFirebase(String name, String lastName, String idNumber, String dob, String issue, String expiry) {
        // Get the Firebase reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Database");

        // Create a new node with the idNumber path and save the data
        DatabaseReference userReference = databaseReference.child(idNumber);
        userReference.child("Name").setValue(name);
        userReference.child("Last Name").setValue(lastName);
        userReference.child("Identification Number").setValue(idNumber);
        userReference.child("Date Of Birth").setValue(dob);
        userReference.child("Date Of Issue").setValue(issue);
        userReference.child("Date Of Expiry").setValue(expiry);

        // Display a success message or navigate back to the previous activity
        Toast.makeText(EditActivity.this, "Data saved successfully", Toast.LENGTH_SHORT).show();

        // Finish the current activity
        finish();
    }

    // Method to hide the keyboard
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}