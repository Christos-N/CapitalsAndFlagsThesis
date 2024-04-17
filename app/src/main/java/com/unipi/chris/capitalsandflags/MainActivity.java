package com.unipi.chris.capitalsandflags;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Button switchButton;
    private FirebaseAuth mAuth;
    private ActionBar actionBar;
    private boolean doubleBackToExitPressedOnce = false;
    private static final int BACK_PRESS_INTERVAL = 3000; // 3 seconds


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        mAuth = FirebaseAuth.getInstance();
        switchButton = findViewById(R.id.switchButton);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, R.string.switched_to_admin, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, AdminViewUsersActivity.class);
                startActivity(intent);
                finish();
            }
        });
        checkIfAdmin();

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutMessage();
            }
        });

        Button viewTablesButton = findViewById(R.id.viewTablesButton);
        viewTablesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViewTablesActivity();
            }
        });
        Button openChooseButton = findViewById(R.id.openQuizButton);
        openChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChooseActivity();
            }
        });
        DataHandler dataHandler = new DataHandler(this);
        dataHandler.checkImagesExistInLocalStorage("flags", false);
        dataHandler.checkImagesExistInLocalStorage("places", false);
    }
    public void openViewTablesActivity() {
        Intent intent = new Intent(this, ViewTablesActivity.class);
        startActivity(intent);
    }
    public void startChooseActivity() {
        if (UtilityClass.isConnectedToInternet(this)) {
            Intent intent = new Intent(this, ChooseContinentOrSavedActivity.class);
            startActivity(intent);
        }
        else
            Toast.makeText(this, R.string.please_connect, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finishAffinity(); // Close app
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getResources().getString(R.string.back_again), Toast.LENGTH_SHORT).show();

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, BACK_PRESS_INTERVAL);
    }
    @Override
    protected void onResume() {
        super.onResume();
        actionBar.setTitle(R.string.welcome);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quiz_menu, menu);
        MenuItem statisticsItem = menu.findItem(R.id.statisticsIcon);
        MenuItem leaderboardItem = menu.findItem(R.id.leaderboardIcon);
        MenuItem helpItem = menu.findItem(R.id.helpIcon);


        statisticsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                if (UtilityClass.isConnectedToInternet(MainActivity.this)) {
                    Intent intent = new Intent(MainActivity.this, ShowStatsActivity.class);
                    intent.putExtra("continent", "everything");
                    startActivity(intent);
                }
                else
                    Toast.makeText(MainActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        leaderboardItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                if (UtilityClass.isConnectedToInternet(MainActivity.this)) {
                    Intent intent = new Intent(MainActivity.this, ShowLeaderboardActivity.class);
                    intent.putExtra("continent", "everything");
                    startActivity(intent);
                }
                else
                    Toast.makeText(MainActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        helpItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, ShowHelpActivity.class);
                intent.putExtra("activityName", "Main");
                startActivity(intent);
                return true;
            }
        });
        return true;
    }

    private void showLogoutMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.logout)
                .setMessage(R.string.question_logout)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logoutUser();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void logoutUser() {
        DataHandler dataHandler = new DataHandler(this);
        String[] continents;
        continents = new String[]{"All countries", "Europe", "Africa", "North America", "South America", "Asia", "Oceania", "Saved Flags", "Saved Capitals"};

        for (int gameMode = 1; gameMode <= 4; gameMode++) {
            for (String continent : continents) {
                // Check if there is saved progress
                String sharedPreferencesName = getSharedPreferencesName(String.valueOf(gameMode), continent);
                SharedPreferences sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
                String answeredCountries = sharedPreferences.getString("answeredCountries", "");

                if (!answeredCountries.isEmpty()) {
                    // Clear saved progress
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                    if (!continent.equals("Saved Flags") && !continent.equals("Saved Capitals"))
                        // Increase the counter in the database
                        dataHandler.increaseTotalCounter(continent, gameMode);
                }
            }

        }
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    private String getSharedPreferencesName(String quizType, String continent) {
        return "QuizProgress_" + quizType + "_" + continent;
    }
    public void checkIfAdmin() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userEmail);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // dataSnapshot contains the data of the user
                        Boolean isAdmin = dataSnapshot.child("isAdmin").getValue(Boolean.class);
                        if (isAdmin != null && isAdmin) {
                            // User is an admin
                            switchButton.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}