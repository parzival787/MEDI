package com.example.medi; // Package declaration for app namespace

import androidx.activity.result.ActivityResultLauncher; // For launching activities and receiving results
import androidx.activity.result.contract.ActivityResultContracts; // Predefined contracts for activity results
import androidx.appcompat.app.AppCompatActivity; // Base class for activities using AppCompat support
import androidx.recyclerview.widget.LinearLayoutManager; // Layout manager for RecyclerView (vertical scrolling)
import androidx.recyclerview.widget.RecyclerView; // RecyclerView widget

import android.content.Intent; // Used for launching other activities or picking media
import android.graphics.Bitmap; // Bitmap representation of images
import android.net.Uri; // URI reference to files, images, etc.
import android.os.Bundle; // Bundle for passing data to activities
import android.provider.MediaStore; // Access device media content
import android.util.Log; // Logging
import android.view.View; // Basic UI view
import android.widget.Button; // UI button
import android.widget.ImageView; // UI image view
import android.widget.ProgressBar; // UI progress bar
import android.widget.TextView; // UI text view
import android.widget.Toast; // For short messages to user

import com.google.android.gms.tasks.Task; // Represents asynchronous operations
import com.google.android.gms.tasks.Tasks; // Utilities for handling multiple Tasks
import com.google.firebase.auth.FirebaseAuth; // Firebase authentication
import com.google.firebase.auth.FirebaseUser; // Represents a signed-in user
import com.google.mlkit.vision.common.InputImage; // ML Kit input image for OCR
import com.google.mlkit.vision.text.TextRecognition; // ML Kit text recognition client
import com.google.mlkit.vision.text.latin.TextRecognizerOptions; // Default Latin text recognition options
import com.google.firebase.firestore.FirebaseFirestore; // Firestore database access
import com.google.firebase.firestore.Query; // Firestore query object
import com.google.firebase.firestore.QueryDocumentSnapshot; // Firestore document snapshot in query results
import com.google.firebase.firestore.QuerySnapshot; // Collection of query results

import java.io.IOException; // Exception for file/image IO
import java.util.ArrayList; // Resizable list
import java.util.HashSet; // Set implementation to remove duplicates
import java.util.List; // Interface for lists
import java.util.Set; // Interface for sets
import java.util.regex.Matcher; // Regex matcher
import java.util.regex.Pattern; // Regex pattern

public class Uploadprescription extends AppCompatActivity { // Activity for uploading prescription images

    // --- UI Components ---
    private Button btnSelectImage, btnRunOCR; // Buttons for selecting image and running OCR
    private ImageView imagePreview; // Image preview UI
    private RecyclerView recyclerViewOcrResults; // RecyclerView to display OCR results
    private ProgressBar progressBarOcr; // Progress indicator while OCR/searching
    private TextView tvResultsLabel, tvNoResults; // TextViews for labeling results or showing "no results"

    // --- State Variables ---
    private Uri imageUri; // URI of selected image
    private FirebaseFirestore db; // Firestore instance
    private OcrResultsAdapter adapter; // Adapter for displaying OCR results

    private ActivityResultLauncher<Intent> imagePickerLauncher; // Launcher for image picker activity

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Activity entry point
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploadprescription); // Inflate layout XML

        db = FirebaseFirestore.getInstance(); // Initialize Firestore instance

        // Initialize UI references
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnRunOCR = findViewById(R.id.btnRunOCR);
        imagePreview = findViewById(R.id.imagePreview);
        recyclerViewOcrResults = findViewById(R.id.recyclerViewOcrResults);
        progressBarOcr = findViewById(R.id.progressBarOcr);
        tvResultsLabel = findViewById(R.id.tvResultsLabel);
        tvNoResults = findViewById(R.id.tvNoResults);

        recyclerViewOcrResults.setLayoutManager(new LinearLayoutManager(this)); // Set vertical list layout for RecyclerView

        btnRunOCR.setEnabled(false); // Disable OCR button initially until image is selected

        // Setup image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), // Contract for starting activity with a result
                result -> { // Callback after activity finishes
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) { // Check if image selected
                        imageUri = result.getData().getData(); // Store selected image URI
                        imagePreview.setImageURI(imageUri); // Display selected image
                        btnRunOCR.setEnabled(true); // Enable OCR button
                        // Clear previous results
                        recyclerViewOcrResults.setVisibility(View.GONE);
                        tvResultsLabel.setVisibility(View.GONE);
                        tvNoResults.setVisibility(View.GONE);
                    }
                });

        // Button click listener to select an image
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // Open gallery
            imagePickerLauncher.launch(intent); // Launch image picker
        });

        // Button click listener to run OCR
        btnRunOCR.setOnClickListener(v -> {
            if (imageUri != null) { // Ensure an image is selected
                runTextRecognition(); // Perform OCR
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show(); // Prompt user
            }
        });
    }

    private void runTextRecognition() { // Perform OCR on selected image
        progressBarOcr.setVisibility(View.VISIBLE); // Show progress bar
        recyclerViewOcrResults.setVisibility(View.GONE); // Hide previous results
        tvResultsLabel.setVisibility(View.GONE); // Hide previous results label
        tvNoResults.setVisibility(View.GONE); // Hide "no results" text

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri); // Load image as Bitmap
            InputImage image = InputImage.fromBitmap(bitmap, 0); // Convert to ML Kit InputImage
            com.google.mlkit.vision.text.TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS); // Initialize recognizer

            recognizer.process(image) // Run OCR
                    .addOnSuccessListener(visionText -> { // Success callback
                        String ocrText = visionText.getText(); // Get recognized text
                        Log.d("OCR", "Raw Text: " + ocrText); // Log raw OCR text
                        processOcrTextAndSearchFirestore(ocrText); // Process text and search Firestore
                    })
                    .addOnFailureListener(e -> { // Failure callback
                        progressBarOcr.setVisibility(View.GONE); // Hide progress
                        Toast.makeText(this, "OCR Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show(); // Show error
                    });
        } catch (IOException e) { // Handle image loading errors
            progressBarOcr.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Extract potential medicine names from OCR text
    private Set<String> extractPotentialMedicineNames(String ocrText) {
        Set<String> potentialNames = new HashSet<>(); // Use a set to avoid duplicates
        String[] lines = ocrText.split("\\r?\\n"); // Split text by lines
        Pattern wordPattern = Pattern.compile("[a-zA-Z]{4,}"); // Regex for words with 4+ letters

        for (String line : lines) {
            Matcher matcher = wordPattern.matcher(line); // Find all matches in line
            while (matcher.find()) {
                String word = matcher.group(); // Extract matched word
                if (word.length() > 3) { // Filter words too short
                    String potentialName = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase(); // Normalize capitalization
                    potentialNames.add(potentialName); // Add to set
                }
            }
        }
        Log.d("OCR", "Potential Names Extracted: " + potentialNames); // Debug log
        return potentialNames; // Return set of potential medicine names
    }

    private void processOcrTextAndSearchFirestore(String ocrText) { // Search Firestore using extracted words
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser(); // Get currently logged-in user
        if (currentUser == null) { // Ensure user is authenticated
            progressBarOcr.setVisibility(View.GONE); // Hide progress
            Toast.makeText(this, "Error: User is not signed in. Cannot search.", Toast.LENGTH_LONG).show(); // Show error
            Log.e("FirestoreSearch", "User is null, permission will be denied."); // Log error
            tvResultsLabel.setVisibility(View.VISIBLE);
            tvNoResults.setVisibility(View.VISIBLE);
            recyclerViewOcrResults.setVisibility(View.GONE);
            return; // Stop execution
        }
        Log.d("FirestoreSearch", "User is authenticated: " + currentUser.getUid()); // Log user ID

        Set<String> potentialNames = extractPotentialMedicineNames(ocrText); // Get potential medicine names

        if (potentialNames.isEmpty()) { // Handle no names found
            progressBarOcr.setVisibility(View.GONE);
            tvResultsLabel.setVisibility(View.VISIBLE);
            tvNoResults.setVisibility(View.VISIBLE);
            recyclerViewOcrResults.setVisibility(View.GONE);
            Toast.makeText(this, "No potential medicine names found in text.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Task<QuerySnapshot>> searchTasks = new ArrayList<>(); // List of Firestore query tasks

        for (String name : potentialNames) { // Create a query for each potential name
            Query query = db.collectionGroup("my_inventory") // Query across all inventories
                    .orderBy("name") // Order by medicine name
                    .startAt(name) // Start at current word
                    .endAt(name + '\uf8ff') // End at word prefix (for partial match)
                    .limit(3); // Limit results
            searchTasks.add(query.get()); // Add task to list
        }

        // Wait for all search tasks to complete
        Tasks.whenAllSuccess(searchTasks.toArray(new Task[0]))
                .addOnSuccessListener(resultsList -> { // When all queries succeed
                    progressBarOcr.setVisibility(View.GONE); // Hide progress
                    List<QueryDocumentSnapshot> combinedResults = new ArrayList<>();

                    for (Object result : resultsList) { // Combine results from all queries
                        if (result instanceof QuerySnapshot) {
                            for (QueryDocumentSnapshot doc : (QuerySnapshot) result) { // Add each document
                                combinedResults.add(doc);
                            }
                        }
                    }

                    // Remove duplicates by document ID
                    Set<String> seenIds = new HashSet<>();
                    List<QueryDocumentSnapshot> uniqueResults = new ArrayList<>();
                    for(QueryDocumentSnapshot doc : combinedResults){
                        if(seenIds.add(doc.getId())){ // Add if not seen before
                            uniqueResults.add(doc);
                        }
                    }

                    if (uniqueResults.isEmpty()) { // No matches found
                        tvResultsLabel.setVisibility(View.VISIBLE);
                        tvNoResults.setVisibility(View.VISIBLE);
                        recyclerViewOcrResults.setVisibility(View.GONE);
                    } else { // Display results
                        Log.d("FirestoreSearch", "Found " + uniqueResults.size() + " unique potential matches.");
                        displayFirestoreResults(uniqueResults);
                    }
                })
                .addOnFailureListener(e -> { // If queries fail
                    progressBarOcr.setVisibility(View.GONE); // Hide progress
                    tvResultsLabel.setVisibility(View.VISIBLE);
                    tvNoResults.setVisibility(View.VISIBLE); // Show no results
                    recyclerViewOcrResults.setVisibility(View.GONE);
                    Log.e("FirestoreSearch", "Error performing collection group search", e); // Log error
                    Toast.makeText(this, "Error searching inventory: " + e.getMessage(), Toast.LENGTH_LONG).show(); // Show error
                });
    }

    private void displayFirestoreResults(List<QueryDocumentSnapshot> documents) { // Display search results in RecyclerView
        adapter = new OcrResultsAdapter(documents); // Initialize adapter with documents
        recyclerViewOcrResults.setAdapter(adapter); // Set adapter to RecyclerView
        tvResultsLabel.setVisibility(View.VISIBLE); // Show results label
        tvNoResults.setVisibility(View.GONE); // Hide no results
        recyclerViewOcrResults.setVisibility(View.VISIBLE); // Show results
    }
}
