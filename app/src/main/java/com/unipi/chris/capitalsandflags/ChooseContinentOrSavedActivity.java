package com.unipi.chris.capitalsandflags;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseContinentOrSavedActivity extends AppCompatActivity {

    TextView allCountriesTextView, europeTextView, africaTextView, northAmericaTextView, southAmericaTextView, asiaTextView, oceaniaTextView;
    Button allCountriesButton, europeButton, africaButton, northAmericaButton, southAmericaButton, asiaButton, oceaniaButton, savedCapitalsButton, savedFlagsButton;
    ActionBar actionBar;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_continent_or_saved);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        allCountriesTextView = findViewById(R.id.allCountriesProgressText);
        europeTextView = findViewById(R.id.europeProgressText);
        africaTextView = findViewById(R.id.africaProgressText);
        northAmericaTextView = findViewById(R.id.northAmericaProgressText);
        southAmericaTextView = findViewById(R.id.southAmericaProgressText);
        asiaTextView = findViewById(R.id.asiaProgressText);
        oceaniaTextView = findViewById(R.id.oceaniaProgressText);

        allCountriesButton = findViewById(R.id.allCountriesButton);
        europeButton = findViewById(R.id.europeButton);
        africaButton = findViewById(R.id.africaButton);
        northAmericaButton = findViewById(R.id.northAmericaButton);
        southAmericaButton = findViewById(R.id.SouthAmericaButton);
        asiaButton = findViewById(R.id.asiaButton);
        oceaniaButton = findViewById(R.id.oceaniaButton);
        savedCapitalsButton = findViewById(R.id.savedCapitals);
        savedFlagsButton = findViewById(R.id.savedFlags);

        progressBar = findViewById(R.id.progressBar);

        allCountriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseQuizType(getString(R.string.all_countries));
            }
        });
        europeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseQuizType(getString(R.string.europe));
            }
        });

        africaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseQuizType(getString(R.string.africa));
            }
        });

        northAmericaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseQuizType(getString(R.string.north_america));
            }
        });

        southAmericaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseQuizType(getString(R.string.south_america));
            }
        });

        asiaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseQuizType(getString(R.string.asia));
            }
        });

        oceaniaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseQuizType(getString(R.string.oceania));
            }
        });
        savedCapitalsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UtilityClass.isConnectedToInternet(ChooseContinentOrSavedActivity.this)) {
                    DataHandler dataHandler = new DataHandler(ChooseContinentOrSavedActivity.this);
                    dataHandler.startSavedChooseQuiz("Saved Capitals");
                }
                else
                    Toast.makeText(ChooseContinentOrSavedActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
            }
        });
        savedFlagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UtilityClass.isConnectedToInternet(ChooseContinentOrSavedActivity.this)) {
                    DataHandler dataHandler = new DataHandler(ChooseContinentOrSavedActivity.this);
                    dataHandler.startSavedChooseQuiz("Saved Flags");
                }
                else
                    Toast.makeText(ChooseContinentOrSavedActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateStats(){
        DataHandler dataHandler = new DataHandler(this);
        dataHandler.updateTextViewForContinent("all countries", allCountriesTextView);
        dataHandler.updateTextViewForContinent("europe", europeTextView);
        dataHandler.updateTextViewForContinent("asia", asiaTextView);
        dataHandler.updateTextViewForContinent("africa", africaTextView);
        dataHandler.updateTextViewForContinent("oceania", oceaniaTextView);
        dataHandler.updateTextViewForContinent("south america", southAmericaTextView);
        dataHandler.updateTextViewForContinent("north america", northAmericaTextView);
        dataHandler.getTotalSumOfGameModes(progressBar);
        if (getIntent().getBooleanExtra("passed", false))
            showMessage(getResources().getString(R.string.congratulations), getResources().getString(R.string.success_all_levels), getResources().getString(R.string.thanks));
    }
    private void showMessage(String title, String message, String answer){
        // Create an instance of AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the title and message of the dialog
        builder.setTitle(title)
                .setMessage(message);

        // Add a button to the dialog
        builder.setPositiveButton(answer, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Close the dialog
            }
        });
        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actionBar.setTitle(getResources().getString(R.string.choose_continent));
        updateStats();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    public void chooseQuizType(String continent){
        if (UtilityClass.isConnectedToInternet(this)) {
            Intent intent = new Intent(ChooseContinentOrSavedActivity.this, ChooseQuizActivity.class);
            intent.putExtra("continent", continent);
            startActivity(intent);
        }
        else
            Toast.makeText(ChooseContinentOrSavedActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChooseContinentOrSavedActivity.this, MainActivity.class);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quiz_menu, menu);
        MenuItem helpItem = menu.findItem(R.id.helpIcon);
        MenuItem statisticsItem = menu.findItem(R.id.statisticsIcon);
        MenuItem leaderboardItem = menu.findItem(R.id.leaderboardIcon);
        helpItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(ChooseContinentOrSavedActivity.this, ShowHelpActivity.class);
                intent.putExtra("activityName", "ChooseContinentOrSaved");
                startActivity(intent);
                return true;
            }
        });
        statisticsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                if (UtilityClass.isConnectedToInternet(ChooseContinentOrSavedActivity.this)) {
                    Intent intent = new Intent(ChooseContinentOrSavedActivity.this, ShowStatsActivity.class);
                    intent.putExtra("continent", "everything");
                    startActivity(intent);
                }
                else
                    Toast.makeText(ChooseContinentOrSavedActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        leaderboardItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                if (UtilityClass.isConnectedToInternet(ChooseContinentOrSavedActivity.this)) {
                    Intent intent = new Intent(ChooseContinentOrSavedActivity.this, ShowLeaderboardActivity.class);
                    intent.putExtra("continent", "everything");
                    startActivity(intent);
                }
                else
                    Toast.makeText(ChooseContinentOrSavedActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        return true;
    }
}