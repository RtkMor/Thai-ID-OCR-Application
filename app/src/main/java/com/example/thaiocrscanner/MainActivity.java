package com.example.thaiocrscanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 200;
    private static final int MULTIPLE_IMAGES_REQUEST_CODE = 300;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        FirebaseApp.initializeApp(this);

        // Initialize SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        // Retrieve night mode preference
        boolean isNightModeEnabled = sharedPreferences.getBoolean(Constants.NIGHT_MODE_ENABLED, false);

        // Set night mode based on the preference
        AppCompatDelegate.setDefaultNightMode(isNightModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);


        // Open Camera
        findViewById(R.id.selectCameraBtn).setOnClickListener(v -> openCamera());

        // Open Gallery to Select Single Image
        findViewById(R.id.selectImageBtn).setOnClickListener(v -> openGalleryForSingleImage());

        // Open Gallery to Select Multiple Images
        findViewById(R.id.selectMultipleImageBtn).setOnClickListener(v -> openGalleryForMultipleImages());

        // Perform Crud Operation Button
        findViewById(R.id.performCrudOperationBtn).setOnClickListener(v -> {
            Intent intent = new Intent(this,CrudActivity.class);
            startActivity(intent);
        });

        // Add Data Manually Button
        findViewById(R.id.addDataManuallyBtn).setOnClickListener(v -> {
            Intent intent = new Intent(this,EditActivity.class);
            startActivity(intent);
        });

        // Settings Button
        findViewById(R.id.settingsIcon).setOnClickListener(v -> {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Camera result
        // If you rotate the camera or anything then it will become difficult to process image
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            assert data != null;
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Toast.makeText(this, "Processing captured image", Toast.LENGTH_SHORT).show();
            InputImage image = InputImage.fromBitmap(photo,0);
            TextRecognizer(image);
        }

        // Single Image From Gallery result
        // Provide image in portrait mode if possible
        else if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            // Check image size
            long imageSize = getImageSize(selectedImageUri);
            if (imageSize <= 2*1024*1024) {
                // Image is less than 2MB so proceed with Text Recognizer
                InputImage image;
                try {
                    image = InputImage.fromFilePath(this, selectedImageUri);
                    TextRecognizer(image);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(this, "Processing " + imageSize/1024 + "KB image", Toast.LENGTH_SHORT).show();
            } else {
                // Image size exceeds the limit
                Toast.makeText(this, "Selected image size exceeds 2MB limit", Toast.LENGTH_SHORT).show();
            }
        }

        // Multiple Images From Gallery result
        // Multiple instances of Alert Dialog box will appear for each image
        // provide me little more time, to fix this issue and show all data at once
        else if (requestCode == MULTIPLE_IMAGES_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                // User selected multiple images
                ClipData clipData = data.getClipData();
                int count = clipData.getItemCount();
                // Display count of images under processing
                Toast.makeText(this, "Processing " + count + " images", Toast.LENGTH_SHORT).show();
                // Initialize temp to count the failed image processing
                int temp = 0;
                for (int i = 0; i < count; i++) {
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    // Check image size
                    long imageSize = getImageSize(imageUri);
                    if (imageSize > 2 * 1024 * 1024) {
                        // Image greater than 2MB so increase temp count
                        temp++;
                    } else {
                        // Perform actions with each selected image
                        InputImage image;
                        try {
                            image = InputImage.fromFilePath(this, imageUri);
                            TextRecognizer(image);
                        } catch (IOException e) {
                            temp++;
                            e.printStackTrace();
                        }
                    }
                }

                // Display an error message to display the count of failed image processes
                if (temp > 0) {
                    if(temp==1) Toast.makeText(this, "Failed to Process " + temp + " image", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(this, "Failed to Process " + temp + " images", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(this, "Select more than 1 image", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void TextRecognizer(InputImage image){

        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    // Task completed successfully
                    showAlertDialog(visionText.getText());
                })
                .addOnFailureListener(
                        e -> {
                            // Task failed with an exception
                            // Handle the failure
                        });

    }

    private void showAlertDialog(String resultText) {
        String[] lines = resultText.split("\n");

        String name = "";
        String lastName = "";
        String idNumber = "";
        String dob = "";
        String dateOfIssue = "";
        String dateOfExpiry = "";

        // Point to be noted -> I am processing image based on the sample image provided to us
        // if you want me to build a robust ML kit to recognize text
        // provide me little more time to study this field, atleast 2-3 more days

        // Processing the captured text to extract only useful information
        for (int i = 0; i < lines.length; i++) {
            // Extracting Name
            if (lines[i].contains("Name") && lines[i].indexOf("Name") == 0) {
                int startIndex = "Name".length() + 1;
                int endIndex = lines[i].length();
                if (endIndex >= startIndex) {
                    name = lines[i].substring(startIndex, endIndex).trim();
                }
            }
            // Extracting Last Name
            else if (lines[i].contains("Last") && i>=1) {
                lastName = lines[i-1];
            }
            // Extracting Identification Number
            else if (lines[i].length() >= 15 && lines[i].matches("[0-9 ]+")) {
                idNumber = lines[i].replaceAll("\\s", "");
            }
            // Extracting Date Of Birth
            else if (lines[i].contains("Birth")) {
                int startIndex = "Date Of Birth".length() + 1;
                int endIndex = lines[i].length();
                if (endIndex >= startIndex) {
                    dob = lines[i].substring(startIndex, endIndex).trim();
                }
            }
            // Extracting Date Of Issue
            else if (lines[i].contains("ssue") && lines[i].contains("Date") && i>=1) {
                dateOfIssue = lines[i-1];
            }
            // Extracting Date Of Expiry
            else if (lines[i].contains("Exp") && lines[i].contains("Date") && i>=1) {
                dateOfExpiry = lines[i-1];
            }

        }

        // Now you have extracted values, display them in the dialog box in the json format
        String message = "{\n\tName: \"" + name + "\",\n"
                + "\tLast Name: \"" + lastName + "\",\n"
                + "\tID Number: \"" + idNumber + "\",\n"
                + "\tDate of Birth: \"" + dob + "\",\n"
                + "\tDate of Issue: \"" + dateOfIssue + "\",\n"
                + "\tDate of Expiry: \"" + dateOfExpiry + "\"\n}";

        // Build an AlertDialog to display the extracted information
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ID Information");
        builder.setMessage(message);
        String finalName = name;
        String finalLastName = lastName;
        String finalIdNumber = idNumber;
        String finalDob = dob;
        String finalDateOfIssue = dateOfIssue;
        String finalDateOfExpiry = dateOfExpiry;
        builder.setPositiveButton("Save", (dialog, which) -> {
            // On clicking the save button the data will get saved in the database
            saveData(finalName, finalLastName, finalIdNumber, finalDob, finalDateOfIssue, finalDateOfExpiry);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Handle cancel button click
            dialog.dismiss();
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        if (!name.isEmpty() && !lastName.isEmpty() && !idNumber.isEmpty() && !dob.isEmpty() && !dateOfExpiry.isEmpty() && !dateOfIssue.isEmpty()) {
            alertDialog.show();
        } else {
            Toast.makeText(this, "Failed to process the image, some fields are empty!", Toast.LENGTH_SHORT).show();
        }

    }

    private void saveData(String finalName, String finalLastName, String finalIdNumber, String finalDob, String finalDateOfIssue, String finalDateOfExpiry) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Database");

        // Check if the parent node "Database" exists
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // "Database" node doesn't exist, create it
                    databaseReference.setValue("");
                    Toast.makeText(MainActivity.this, "Data successfully saved in the Database!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Data already present in the Database!", Toast.LENGTH_SHORT).show();
                }
                // Continue with saving data to the specified path
                saveDataToSpecifiedPath(finalName, finalLastName, finalIdNumber, finalDob, finalDateOfIssue, finalDateOfExpiry);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Save Data", "Database Error");
                Toast.makeText(MainActivity.this, "Failed to update Database!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDataToSpecifiedPath(String finalName, String finalLastName, String finalIdNumber, String finalDob, String finalDateOfIssue, String finalDateOfExpiry) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Database").child(finalIdNumber);

        // Create a HashMap to store the data
        HashMap<String, Object> idData = new HashMap<>();
        idData.put("Name", finalName);
        idData.put("Last Name", finalLastName);
        idData.put("Identification Number", finalIdNumber);
        idData.put("Date Of Birth", finalDob);
        idData.put("Date Of Issue", finalDateOfIssue);
        idData.put("Date Of Expiry", finalDateOfExpiry);

        // Push the data to Firebase using the new book ID
        databaseReference.setValue(idData);
    }

    // Method to get the size of the selected image to check whether selected image is greater than 2MB or not
    private long getImageSize(Uri uri) {
        if (uri == null) {
            // Handle the case where the URI is null
            return 0;
        }

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);

        if (cursor != null) {
            try {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

                if (columnIndex >= 0) {
                    String filePath = cursor.getString(columnIndex);

                    if (filePath != null && !filePath.isEmpty()) {
                        File file = new File(filePath);
                        return file.length(); // It will return the size in bytes
                    }
                }
            } finally {
                cursor.close();
            }
        }

        return 0;
    }

    // Open Camera
    @SuppressLint("QueryPermissionsNeeded")
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }
    }

    // Select Single Image From Gallery
    @SuppressLint("IntentReset")
    private void openGalleryForSingleImage() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    // Select Multiple Images From Gallery
    private void openGalleryForMultipleImages() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(galleryIntent, MULTIPLE_IMAGES_REQUEST_CODE);
    }

}
