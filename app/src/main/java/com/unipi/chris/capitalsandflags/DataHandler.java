package com.unipi.chris.capitalsandflags;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataHandler {
    private Context context;

    public DataHandler(Context context) {
        this.context = context;
    }
    public void startActivityWithCountryData(String selectedContinent, String capitalOrCountry, boolean load) {
        List<Country> countries = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("countries");

        databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                        String continent = dataSnapshot.child("continent").getValue(String.class);
                        if (selectedContinent.equals(continent) || selectedContinent.equals(context.getResources().getString(R.string.all_countries))) {
                            String name = dataSnapshot.child("country").getValue(String.class);
                            String capital = dataSnapshot.child("capital").getValue(String.class);
                            String filename = dataSnapshot.child("filename").getValue(String.class);

                            // Create a Country object and add it to the list
                            Country country = new Country(name, capital, continent, filename);
                            countries.add(country);
                        }
                    }

                    // Intent creation and additional logic
                    Intent intent;
                    checkImagesExistInLocalStorage("flags", true);
                    checkImagesExistInLocalStorage("places", true);

                    if (capitalOrCountry.equals("guessCapital") || capitalOrCountry.equals("guessCountry") || capitalOrCountry.equals("guessCountryByFlag")) {
                        intent = new Intent(context, QuizActivity.class);
                        intent.putExtra("capitalOrCountry", capitalOrCountry);
                        intent.putExtra("load", load);
                    } else if (capitalOrCountry.equals("guessFlagByCountry")) {
                        intent = new Intent(context, FlagQuizActivity.class);
                        intent.putExtra("capitalOrCountry", capitalOrCountry);
                        intent.putExtra("load", load);
                    } else {
                        intent = new Intent(context, CountryListActivity.class);
                    }

                    // Intent extras
                    intent.putExtra("continent", selectedContinent);
                    intent.putExtra("list", (Serializable) countries);
                    context.startActivity(intent);
                }
            }
        });
    }

    public void startActivityWithSavedCountryData(String tableName, boolean exitToViewTables, String capitalOrCountry, boolean load) {      //exitToViewTables is true whenever user has to be redirected to ViewTables in case there are no saved Countries (they deleted each one)
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ","); //capitalOrCountry is not "" whenever we need to fetch the data and set it for the quiz (Saved Capitals or Saved Flags)
        
        DatabaseReference userFlagsReference = FirebaseDatabase.getInstance().getReference("users").child(userEmail).child(tableName);

        List<Country> countries = new ArrayList<>();
        // Fetch the list of country names from "users -> email -> Saved Flags"
        userFlagsReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    List<String> countryNames = new ArrayList<>();

                    for (DataSnapshot countrySnapshot : dataSnapshot.getChildren()) {
                        String countryName = countrySnapshot.getValue(String.class);
                        if (countryName != null) {
                            countryNames.add(countryName);
                        }
                    }

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("countries");
                    databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                                for (String countryName : countryNames) {
                                    for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                                        String filename = dataSnapshot.child("filename").getValue(String.class);
                                        if (filename.equals(countryName)) {
                                            String name = dataSnapshot.child("country").getValue(String.class);
                                            String continent = dataSnapshot.child("continent").getValue(String.class);
                                            String capital = dataSnapshot.child("capital").getValue(String.class);

                                            // Create a Country object and add it to the list
                                            Country country = new Country(name, capital, continent, filename);
                                            countries.add(country);
                                        }
                                    }
                                }
                                checkImagesExistInLocalStorage("flags", true);
                                checkImagesExistInLocalStorage("places", true);
                                //Start CountryListActivity if there is at least one country
                                if (!countries.isEmpty() && capitalOrCountry.equals("")) {
                                    Intent intent = new Intent(context, CountryListActivity.class);
                                    intent.putExtra("continent", tableName);
                                    intent.putExtra("list", (Serializable) countries);
                                    context.startActivity(intent);
                                }
                                else if(!countries.isEmpty() && capitalOrCountry.equals("guessFlagByCountry")){
                                    Intent intent = new Intent(context, FlagQuizActivity.class);
                                    intent.putExtra("continent", tableName);
                                    intent.putExtra("list", (Serializable) countries);
                                    intent.putExtra("capitalOrCountry", capitalOrCountry);
                                    intent.putExtra("load", load);
                                    context.startActivity(intent);
                                }
                                else if(!countries.isEmpty()){
                                    Intent intent = new Intent(context, QuizActivity.class);
                                    intent.putExtra("continent", tableName);
                                    intent.putExtra("list", (Serializable) countries);
                                    intent.putExtra("capitalOrCountry", capitalOrCountry);
                                    intent.putExtra("load", load);
                                    context.startActivity(intent);
                                }
                                else if(!capitalOrCountry.equals("")){
                                    Toast.makeText(context, context.getResources().getString(R.string.at_least_five_countries), Toast.LENGTH_SHORT).show();
                                }
                                else if(exitToViewTables){
                                    Toast.makeText(context, context.getResources().getString(R.string.at_least_one), Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Intent intent = new Intent(context, ViewTablesActivity.class);
                                    context.startActivity(intent);
                                }
                            }

                        }
                    });
                } else {
                    Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void saveCountry(String flagImage, String tableName){
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userEmail).child(tableName);
        // Fetch the existing saved countries
        databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    boolean countryExists = false;

                    for (DataSnapshot countrySnapshot : dataSnapshot.getChildren()) {
                        String existingCountry = countrySnapshot.getValue(String.class);

                        if (existingCountry != null && existingCountry.equals(flagImage)) {
                            countryExists = true;
                            break;
                        }
                    }

                    if (!countryExists) {
                        // The country does not exist in the list, so add it
                        /*String newIndex = String.valueOf(dataSnapshot.getChildrenCount()); // Get the next index
                        databaseReference.child(newIndex).setValue(flagImage);*/
                        String newKey = databaseReference.push().getKey();
                        // Save the flagImage under the unique key
                        databaseReference.child(newKey).setValue(flagImage);
                        Toast.makeText(context, context.getResources().getString(R.string.country_saved), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.country_already_saved), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void deleteCountry(String flagImage, String tableName) {
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userEmail).child(tableName);

        // Fetch the existing saved countries
        databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    boolean countryExists = false;
                    String keyToDelete = null;

                    for (DataSnapshot countrySnapshot : dataSnapshot.getChildren()) {
                        String existingCountry = countrySnapshot.getValue(String.class);

                        if (existingCountry != null && existingCountry.equals(flagImage)) {
                            countryExists = true;
                            keyToDelete = countrySnapshot.getKey();
                            break;
                        }
                    }

                    if (countryExists) {
                        // The country exists in the list, so delete it
                        databaseReference.child(keyToDelete).removeValue();
                        Toast.makeText(context, context.getResources().getString(R.string.country_deleted), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void startSavedChooseQuiz(String tableName){
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        DatabaseReference userFlagsReference = FirebaseDatabase.getInstance().getReference("users").child(userEmail).child(tableName);

        // Fetch the list of country names from "users -> email -> Saved Flags"
        userFlagsReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    List<String> countryNames = new ArrayList<>();

                    for (DataSnapshot countrySnapshot : dataSnapshot.getChildren()) {
                        String countryName = countrySnapshot.getValue(String.class);
                        if (countryName != null) {
                            countryNames.add(countryName);
                        }
                    }
                    if(countryNames.size() >= 5){
                        Intent intent = new Intent(context, ChooseQuizActivity.class);
                        intent.putExtra("continent", tableName);
                        context.startActivity(intent);
                    }
                    else
                        Toast.makeText(context, context.getResources().getString(R.string.at_least_five_countries), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void initializeUserProgressToDatabase(String email, boolean signup){
        String userEmail = email.replace(".",",");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userEmail);

        // Create a HashMap to hold the progress values
        Map<String, Object> progressMap = new HashMap<>();
        progressMap.put("all countries_mode1", 0);
        progressMap.put("all countries_mode2", 0);
        progressMap.put("all countries_mode3", 0);
        progressMap.put("all countries_mode4", 0);
        progressMap.put("europe_mode1", 0);
        progressMap.put("europe_mode2", 0);
        progressMap.put("europe_mode3", 0);
        progressMap.put("europe_mode4", 0);
        progressMap.put("africa_mode1", 0);
        progressMap.put("africa_mode2", 0);
        progressMap.put("africa_mode3", 0);
        progressMap.put("africa_mode4", 0);
        progressMap.put("north america_mode1", 0);
        progressMap.put("north america_mode2", 0);
        progressMap.put("north america_mode3", 0);
        progressMap.put("north america_mode4", 0);
        progressMap.put("south america_mode1", 0);
        progressMap.put("south america_mode2", 0);
        progressMap.put("south america_mode3", 0);
        progressMap.put("south america_mode4", 0);
        progressMap.put("asia_mode1", 0);
        progressMap.put("asia_mode2", 0);
        progressMap.put("asia_mode3", 0);
        progressMap.put("asia_mode4", 0);
        progressMap.put("oceania_mode1", 0);
        progressMap.put("oceania_mode2", 0);
        progressMap.put("oceania_mode3", 0);
        progressMap.put("oceania_mode4", 0);

        // Set the initial value of CompletedCounter and TotalCounter
        DatabaseReference completedCounterReference = userRef.child("CompletedCounter");
        DatabaseReference totalCounterReference = userRef.child("TotalCounter");

        // Save the progress to CompletedCounter and TotalCounter in the database
        completedCounterReference.setValue(progressMap)
                .addOnSuccessListener(aVoid -> {
                    // Now save the same progress to TotalCounter
                    totalCounterReference.setValue(progressMap)
                            .addOnSuccessListener(aVoid1 -> {
                                // Progress saved to TotalCounter successfully

                                if (signup) {
                                    // Set isAdmin to false
                                    userRef.child("isAdmin").setValue(false);
                                }
                                else {
                                    // Delete content below bestTime
                                    userRef.child("BestTime").removeValue();
                                }
                            });
                });
    }
    public void updateGameModeProgressAndStartActivity(String continent, int gameMode, int bestTime) {
        //Saved Capitals or Saved Flags
        if (continent.equals("Saved Capitals") || continent.equals("Saved Flags")){
            startChooseContinentActivity(false);
            return;
        }
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userEmail).child("CompletedCounter");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int sumOfZeros = 0;

                if (dataSnapshot.exists()) {
                    // Existing logic to calculate sum of zeros
                    for (DataSnapshot progressSnapshot : dataSnapshot.getChildren()) {
                        Integer progressValue = progressSnapshot.getValue(Integer.class);

                        if (progressValue != null && progressValue == 0) {
                            sumOfZeros++;
                        }
                    }

                    String path = continent.toLowerCase() + "_mode" + gameMode;
                    DataSnapshot specificValueSnapshot = dataSnapshot.child(path);

                    if (specificValueSnapshot.exists()) {
                        Integer specificValue = specificValueSnapshot.getValue(Integer.class);

                        if (specificValue != null) {
                            if (specificValue == 0 && sumOfZeros == 1) {
                                // Update the completed counter to "1" and the best time
                                updateCompletedCounterAndBestTime(databaseReference, path, bestTime, true);
                            } else {
                                // Update the completed counter to "1" and the best time
                                updateCompletedCounterAndBestTime(databaseReference, path, bestTime, false);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors
            }
        });
    }
    private void updateCompletedCounterAndBestTime(DatabaseReference reference, String path, int bestTime, boolean completedAll) {
        // Increment the corresponding value for CompletedCounter
        reference.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer currentValue = dataSnapshot.getValue(Integer.class);

                    // Increment the value by 1
                    int incrementedValue = (currentValue != null) ? currentValue + 1 : 1;

                    reference.child(path).setValue(incrementedValue)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Check the existing BestTime in the database
                                    String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
                                    DatabaseReference bestTimeReference = FirebaseDatabase.getInstance().getReference("users")
                                            .child(userEmail).child("BestTime").child(path);

                                    bestTimeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                Integer existingBestTime = dataSnapshot.getValue(Integer.class);

                                                if (existingBestTime != null && existingBestTime < bestTime) {
                                                    // The existing BestTime is greater than the new bestTime
                                                    // No need to update, start activity directly
                                                    startChooseContinentActivity(completedAll);
                                                } else {
                                                    // Update the best time
                                                    bestTimeReference.setValue(bestTime)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    // Start activity based on whether all levels are completed
                                                                    startChooseContinentActivity(completedAll);
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    // Handle failure to update best time
                                                                }
                                                            });
                                                }
                                            } else {
                                                // Data doesn't exist, set the new bestTime directly
                                                bestTimeReference.setValue(bestTime)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                // Start activity based on whether all levels are completed
                                                                startChooseContinentActivity(completedAll);
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                // Handle failure to update best time
                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            // Handle potential errors
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure to update CompletedCounter
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors
            }
        });
    }
    private void startChooseContinentActivity(boolean allPassed){
        Intent intent = new Intent(context, ChooseContinentOrSavedActivity.class);
        intent.putExtra("passed", allPassed);
        context.startActivity(intent);
    }
    public void updateTextViewForContinent(String continent, TextView textView) {
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userEmail).child("CompletedCounter");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalScore = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();

                    // Check if the key contains the continent name and mode
                    if (key != null && key.contains(continent.toLowerCase() + "_mode")) {
                        Integer modeScore = snapshot.getValue(Integer.class);
                        if (modeScore != null && modeScore >= 1) {
                            // If the mode score is 1, increment the total score by 1
                            totalScore++;
                        }
                    }
                }
                //textView.setText(context.getResources().getString(R.string.europe) + ": " + totalScore + "/4");
                String con = "";
                switch (continent){
                    case "all countries":
                        con = context.getResources().getString(R.string.all_countries);
                        break;
                    case "europe":
                        con = context.getResources().getString(R.string.europe);
                        break;
                    case "africa":
                        con = context.getResources().getString(R.string.africa);
                        break;
                    case "north america":
                        con = context.getResources().getString(R.string.north_america);
                        break;
                    case "south america":
                        con = context.getResources().getString(R.string.south_america);
                        break;
                    case "asia":
                        con = context.getResources().getString(R.string.asia);
                        break;
                    case "oceania":
                        con = context.getResources().getString(R.string.oceania);
                        break;
                }
                textView.setText(con + ": " + totalScore + "/4");
                // Here, 'totalScore' contains the total score for the given continent
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that may occur while retrieving data
            }
        });
    }
    public void getTotalSumOfGameModes(ProgressBar progressBar) {
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userEmail).child("CompletedCounter");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalSum = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Integer modeScore = snapshot.getValue(Integer.class);
                    if (modeScore != null && modeScore >= 1) {
                        totalSum++;
                    }
                }
                progressBar.setProgress(totalSum);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that may occur while retrieving data
            }
        });
    }
    public void updateButtonColor(String continent, int mode, Button button) {
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userEmail).child("CompletedCounter");

        String modeKey = continent.toLowerCase() + "_mode" + mode;

        databaseReference.child(modeKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer score = dataSnapshot.getValue(Integer.class);
                if (score  != null && score >= 1) {
                    button.setBackgroundColor(Color.BLACK);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that may occur while retrieving data
            }
        });
    }
    public void increaseTotalCounter(String continent, int modeNumber) {
        //Saved Capitals or Saved Flags
        if (continent.equals("Saved Capitals") || continent.equals("Saved Flags")){
            return;
        }
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        DatabaseReference totalCounterReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userEmail).child("TotalCounter");

        String path = continent.toLowerCase() + "_mode" + modeNumber;

        totalCounterReference.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if the data exists
                if (dataSnapshot.exists()) {
                    Integer currentValue = dataSnapshot.getValue(Integer.class);

                    // Increase the value by 1
                    if (currentValue != null) {
                        int newValue = currentValue + 1;

                        // Update the value in the database
                        totalCounterReference.child(path).setValue(newValue)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Value updated successfully
                                        // Handle success if needed
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle failure to update value
                                    }
                                });
                    }
                } else {
                    Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors
            }
        });
    }
    public void checkImagesExistInLocalStorage(String pathString, boolean showToast) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(pathString);


        storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                boolean missing = false;
                for (StorageReference item : listResult.getItems()) {
                    // Get the image name
                    String imageName = item.getName();
                    // Check if the image file exists in local storage
                    if (!isImageFileExistsLocally(imageName)) {
                        missing = true;
                    }
                }
                if (missing) {
                    downloadImagesFromFirebaseStorage(pathString);
                    if (showToast)
                        Toast.makeText(context, R.string.please_wait_images_loaded, Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    private boolean isImageFileExistsLocally(String imageName) {
        // Check if the image file exists in your local storage directory
        File localImageFile = new File(context.getFilesDir(), imageName);
        return localImageFile.exists();
    }
    private void downloadImagesFromFirebaseStorage(String pathString) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(pathString);

        storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference item : listResult.getItems()) {
                    // Download each file
                    downloadFile(item);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    private void downloadFile(StorageReference storageRef) {
        try {
            File localFile = new File(context.getFilesDir(), storageRef.getName());

            storageRef.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
