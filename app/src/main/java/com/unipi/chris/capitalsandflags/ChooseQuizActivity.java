package com.unipi.chris.capitalsandflags;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ChooseQuizActivity extends AppCompatActivity {

    Button guessCapital, guessCountryCap, guessFlag,guessCountryFlag;
    ActionBar actionBar;
    String continent, continentEnglish;
    boolean flagQuiz = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_quiz);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        guessCapital = findViewById(R.id.guessCapitalButton);
        guessCountryCap = findViewById(R.id.guessCountryByCapitalButton);
        guessCountryFlag = findViewById(R.id.guessCountryByFlagButton);
        guessFlag = findViewById(R.id.guessFlagByCountryButton);

        continent = getIntent().getStringExtra("continent");

        if(continent.equals(getResources().getString(R.string.saved_capitals))){
            guessFlag.setVisibility(View.INVISIBLE);
            guessCountryFlag.setVisibility(View.INVISIBLE);
            continentEnglish = "saved_capitals";
        }
        else if (continent.equals(getResources().getString(R.string.saved_flags))){
            guessCapital.setVisibility(View.INVISIBLE);
            guessCountryCap.setVisibility(View.INVISIBLE);
            continentEnglish = "saved_flags";
        }/*
        else if (continent.equals(getResources().getString(R.string.all_countries))){
            continentEnglish = "all_countries";
            changeFourButtonColors(continentEnglish);
        }
        else if (continent.equals(getResources().getString(R.string.europe))){
            continentEnglish = "europe";
            changeFourButtonColors(continentEnglish);
        }
        else if (continent.equals(getResources().getString(R.string.africa))){
            continentEnglish = "africa";
            changeFourButtonColors(continentEnglish);
        }
        else if (continent.equals(getResources().getString(R.string.north_america))){
            continentEnglish = "north_america";
            changeFourButtonColors(continentEnglish);
        }
        else if (continent.equals(getResources().getString(R.string.south_america))){
            continentEnglish = "south_america";
            changeFourButtonColors(continentEnglish);
        }
        else if (continent.equals(getResources().getString(R.string.asia))){
            continentEnglish = "asia";
            changeFourButtonColors(continentEnglish);
        }
        else if (continent.equals(getResources().getString(R.string.oceania))){
            continentEnglish = "oceania";
            changeFourButtonColors(continentEnglish);
        }*/
        DataHandler dataHandler = new DataHandler(this);
        dataHandler.updateButtonColor(continent, 1, guessCapital);
        dataHandler.updateButtonColor(continent, 2, guessCountryCap);
        dataHandler.updateButtonColor(continent, 3, guessCountryFlag);
        dataHandler.updateButtonColor(continent, 4, guessFlag);

        guessCapital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flagQuiz = false;
                if (!getSharedPreferences(getSharedPreferencesName("1", continent), Context.MODE_PRIVATE)
                        .getString("answeredCountries", "").isEmpty())
                    clearOrLoad(continent, "guessCapital");
                else
                    startQuiz(continent, "guessCapital", false);
            }
        });
        guessCountryCap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flagQuiz = false;
                if (!getSharedPreferences(getSharedPreferencesName("2", continent), Context.MODE_PRIVATE)
                        .getString("answeredCountries", "").isEmpty())
                    clearOrLoad(continent, "guessCountry");
                else
                    startQuiz(continent, "guessCountry", false);
            }
        });
        guessCountryFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flagQuiz = false;
                if (!getSharedPreferences(getSharedPreferencesName("3", continent), Context.MODE_PRIVATE)
                        .getString("answeredCountries", "").isEmpty())
                    clearOrLoad(continent, "guessCountryByFlag");
                else
                    startQuiz(continent, "guessCountryByFlag", false);
            }
        });
        guessFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flagQuiz = true;
                if (!getSharedPreferences(getSharedPreferencesName("4", continent), Context.MODE_PRIVATE)
                        .getString("answeredCountries", "").isEmpty())
                    clearOrLoad(continent, "guessFlagByCountry");
                else
                    startFlagQuiz(continent, "guessFlagByCountry",false);
            }
        });

    }
    private String getSharedPreferencesName(String quizType, String continent) {
        return "QuizProgress_" + quizType + "_" + continent;
    }
    private void clearOrLoad(String continent, String capitalOrCountry) {
        int gameMode;
        switch (capitalOrCountry) {
            case "guessCapital":
                gameMode = 1;
                break;
            case "guessCountry":
                gameMode = 2;
                break;
            case "guessCountryByFlag":
                gameMode = 3;
                break;
            default:
                gameMode = 4;
                break;
        }
        // Create an instance of AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the title and message of the dialog
        builder.setTitle(getResources().getString(R.string.progress_found))
                .setMessage(getResources().getString(R.string.clear_or_load));

        // Add "Yes" button to the dialog
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Close the dialog
                if (!flagQuiz)
                    startQuiz(continent, capitalOrCountry, true);
                else
                    startFlagQuiz(continent, "guessFlagByCountry",true);
            }
        });

        // Add "No" button to the dialog
        builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Close the dialog
                if (!getSharedPreferences(getSharedPreferencesName(String.valueOf(gameMode), continent), Context.MODE_PRIVATE)
                        .getBoolean("educationalMode", false)) {
                    DataHandler dataHandler = new DataHandler(ChooseQuizActivity.this);
                    dataHandler.increaseTotalCounter(continent, gameMode);
                }
                if (!flagQuiz)
                    startQuiz(continent, capitalOrCountry, false);
                else
                    startFlagQuiz(continent, "guessFlagByCountry",false);
            }
        });

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        actionBar.setTitle(continent);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    public void startQuiz(String continent, String capitalOrCountry, boolean load){
        if (UtilityClass.isConnectedToInternet(this)) {
            DataHandler dataHandler = new DataHandler(this);
            if (continent.equals("Saved Capitals") || continent.equals("Saved Flags")) {
                dataHandler.startActivityWithSavedCountryData(continent, false, capitalOrCountry, load);
            } else
                dataHandler.startActivityWithCountryData(continent, capitalOrCountry, load);
        }
        else
            Toast.makeText(ChooseQuizActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
    }
    public void startFlagQuiz(String continent, String capitalOrCountry,boolean load){
        if (UtilityClass.isConnectedToInternet(this)) {
            DataHandler dataHandler = new DataHandler(this);
            if (continent.equals("Saved Capitals") || continent.equals("Saved Flags")) {
                dataHandler.startActivityWithSavedCountryData(continent, false, capitalOrCountry, load);
            } else
                dataHandler.startActivityWithCountryData(continent, capitalOrCountry, load);
        }
        else
            Toast.makeText(ChooseQuizActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quiz_menu, menu);
        MenuItem helpItem = menu.findItem(R.id.helpIcon);
        MenuItem leaderboardItem = menu.findItem(R.id.leaderboardIcon);
        MenuItem statisticsItem = menu.findItem(R.id.statisticsIcon);
        statisticsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                if (UtilityClass.isConnectedToInternet(ChooseQuizActivity.this)) {
                    Intent intent = new Intent(ChooseQuizActivity.this, ShowStatsActivity.class);
                    intent.putExtra("continent", continent);
                    startActivity(intent);
                }
                return true;
            }
        });
        leaderboardItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                if (UtilityClass.isConnectedToInternet(ChooseQuizActivity.this)) {
                    Intent intent = new Intent(ChooseQuizActivity.this, ShowLeaderboardActivity.class);
                    intent.putExtra("continent", continent);
                    startActivity(intent);
                }
                else
                    Toast.makeText(ChooseQuizActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        helpItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(ChooseQuizActivity.this, ShowHelpActivity.class);
                intent.putExtra("activityName", "ChooseQuiz");
                startActivity(intent);
                return true;
            }
        });
        return true;
    }
}