package com.unipi.chris.capitalsandflags;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlagQuizActivity extends AppCompatActivity {

    private List<Country> countryList;
    private List<Country> countryListFull;
    private int currentQuestionIndex;
    private int livesRemaining;
    private int answersRemaining;
    private int answeredQuestionsCount = 0;
    private ImageView option1Flag, option2Flag, option3Flag, option4Flag;
    private TextView selectedCountryTextView;
    private TextView livesRemainingTextView;
    private TextView answersRemainingTextView;
    private ActionBar actionBar;
    private String continent;
    private String continentEnglish;
    private List<Country> answeredCountriesList = new ArrayList<>();
    private boolean educationalMode = false;
    private long startTime;
    private Handler handler;
    private Runnable updateTimeRunnable;

    private final int gameMode = 4;   //4: Flag By Country (1: Capital by Country, 2: Country By Capital, 3: Country By Flag)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flag_quiz);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        selectedCountryTextView = findViewById(R.id.selectedCountryTextView);
        option1Flag = findViewById(R.id.option1Flag);
        option2Flag = findViewById(R.id.option2Flag);
        option3Flag = findViewById(R.id.option3Flag);
        option4Flag = findViewById(R.id.option4Flag);
        livesRemainingTextView = findViewById(R.id.livesRemainingTextView);
        answersRemainingTextView = findViewById(R.id.answersRemainingTextView);

        if (educationalMode){
            livesRemainingTextView.setVisibility(View.INVISIBLE);
            answersRemainingTextView.setVisibility(View.INVISIBLE);
        }

        continent = getIntent().getStringExtra("continent");
        continentEnglish = continent;
        List<Country> receivedList = (List<Country>) getIntent().getSerializableExtra("list");
        countryList = receivedList;
        countryListFull = new ArrayList<>(countryList);

        if (continent.equals(getResources().getString(R.string.saved_capitals)) ||
                continent.equals(getResources().getString(R.string.saved_flags))) {
            answersRemaining = countryList.size();
        } else if (continent.equals(getResources().getString(R.string.all_countries))) {
            answersRemaining = countryList.size();
        } else if (continent.equals(getResources().getString(R.string.south_america))) {
            answersRemaining = 30;
        } else if (continent.equals(getResources().getString(R.string.oceania))) {
            answersRemaining = 30;
        } else if (continent.equals(getResources().getString(R.string.europe)) ||
                continent.equals(getResources().getString(R.string.africa)) ||
                continent.equals(getResources().getString(R.string.north_america)) ||
                continent.equals(getResources().getString(R.string.asia))) {
            answersRemaining = 60;
        }

        Collections.shuffle(countryList);
        updateAnswersRemainingText();

        currentQuestionIndex = 0;
        livesRemaining = 5;
        updateLivesRemainingText();

        if (getIntent().getBooleanExtra("load", false) &&
                getSharedPreferences(getSharedPreferencesName(String.valueOf(gameMode), continentEnglish), MODE_PRIVATE)
                        .getInt("livesRemaining", 5) != 0) {
            answeredCountriesList = loadProgress(livesRemaining, answersRemaining, answeredQuestionsCount);
        }
        else {
            clearProgress();
            startTime = System.currentTimeMillis();
        }
        displayQuestion();
        option1Flag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(option1Flag.getTag().toString());
            }
        });

        option2Flag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(option2Flag.getTag().toString());
            }
        });

        option3Flag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(option3Flag.getTag().toString());
            }
        });

        option4Flag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(option4Flag.getTag().toString());
            }
        });
        handler = new Handler();
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateElapsedTime();
                handler.postDelayed(this, 1000); // Update every 1 second
            }
        };
    }
    private void updateElapsedTime() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        SharedPreferences sharedPreferences = getSharedPreferences(getSharedPreferencesName(String.valueOf(gameMode), continentEnglish), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("time", elapsedTime);
        editor.apply();
    }
    private void saveCurrentCountry() {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < countryList.size()) {
            Country currentCountry = countryList.get(currentQuestionIndex);

            DataHandler dataHandler = new DataHandler(this);
            dataHandler.saveCountry(currentCountry.getFlagImageName(), "Saved Flags");
        }
    }
    public void deleteCurrentCountry(){
        if (currentQuestionIndex >= 0 && currentQuestionIndex < countryList.size()) {
            Country currentCountry = countryList.get(currentQuestionIndex);

            DataHandler dataHandler = new DataHandler(this);
            dataHandler.deleteCountry(currentCountry.getFlagImageName(), "Saved Flags");
        }
    }
    private void displayQuestion() {
        Country currentCountry = countryList.get(currentQuestionIndex);

        List<String> options = new ArrayList<>();

        options.add(currentCountry.getFlagImageName());

        // Add three other random as options
        List<Country> randomCountries = getRandomCountries(currentCountry, 3);
        for (Country country : randomCountries) {
            options.add(country.getFlagImageName());
        }

        Collections.shuffle(options);

        displayImageFromLocalStorage(options.get(0), option1Flag);
        displayImageFromLocalStorage(options.get(1), option2Flag);
        displayImageFromLocalStorage(options.get(2), option3Flag);
        displayImageFromLocalStorage(options.get(3), option4Flag);
        option1Flag.setTag(options.get(0));
        option2Flag.setTag(options.get(1));
        option3Flag.setTag(options.get(2));
        option4Flag.setTag(options.get(3));

        selectedCountryTextView.setText(currentCountry.getName());
        selectedCountryTextView.setVisibility(View.VISIBLE);
    }
    private void displayImageFromLocalStorage(String imageName, ImageView imageView) {
        File localFile = new File(getFilesDir(), imageName+".png");

        if (localFile.exists()) {
            // If the file exists locally, load it into an ImageView
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        }
    }

    private List<Country> getRandomCountries(Country excludeCountry, int count) {
        List<Country> randomCountries = new ArrayList<>(countryListFull);
        randomCountries.remove(excludeCountry);
        Collections.shuffle(randomCountries);
        return randomCountries.subList(0, count);
    }
    private void checkAnswer(String selectedAnswer) {
        if (UtilityClass.isConnectedToInternet(this)) {
            Country currentCountry = countryList.get(currentQuestionIndex);
            if (selectedAnswer.equals(currentCountry.getFlagImageName())) {
                // Correct answer
                if (!educationalMode)
                    answersRemaining--;
                updateAnswersRemainingText();
                answeredQuestionsCount++;

                if (answeredQuestionsCount >= countryListFull.size()) { // User has answered everything, refill database
                    countryList.addAll(countryListFull);
                    answeredCountriesList.clear();
                    Collections.shuffle(countryList);
                    answeredQuestionsCount = 0; // Reset the count
                }

                // Add the current country to the answeredCountriesList
                answeredCountriesList.add(currentCountry);
                countryList.remove(currentCountry); // Remove the current country from countryList

                moveToNextQuestion();
            } else {
                // Incorrect answer, decrement lives
                livesRemaining--;
                updateLivesRemainingText(); // Update lives remaining text

                // Disable the clicked button and change its background color to red
                if (option1Flag.getTag().equals(selectedAnswer)) {
                    option1Flag.setEnabled(false);
                    option1Flag.setAlpha(0.2f);
                } else if (option2Flag.getTag().equals(selectedAnswer)) {
                    option2Flag.setEnabled(false);
                    option2Flag.setAlpha(0.2f);
                } else if (option3Flag.getTag().equals(selectedAnswer)) {
                    option3Flag.setEnabled(false);
                    option3Flag.setAlpha(0.2f);
                } else if (option4Flag.getTag().equals(selectedAnswer)) {
                    option4Flag.setEnabled(false);
                    option4Flag.setAlpha(0.2f);
                }

                if (livesRemaining == 0) {
                    // Out of lives, end the quiz
                    endQuiz(false);
                }
            }
            if (answersRemaining != 0) {
                saveProgress();
            }
        }
        else
            Toast.makeText(this, getResources().getString(R.string.please_connect), Toast.LENGTH_SHORT).show();
    }

    private void updateLivesRemainingText() {
        livesRemainingTextView.setText(getResources().getString(R.string.remaining_lives) + livesRemaining);
    }
    private void updateAnswersRemainingText() {
        answersRemainingTextView.setText(getResources().getString(R.string.remaining_answers) + answersRemaining);
    }

    private void moveToNextQuestion() {
        // Reset button appearance and enable all buttons
        handleImageFlags(true);

        if (answersRemaining == 0){
            endQuiz(true);
        }
        else
            displayQuestion();
    }
    private void endQuiz(boolean win) {
        long time = (System.currentTimeMillis() - startTime)/1000;
        int resultTime = 10000;
        if (time <= Integer.MAX_VALUE)
            resultTime = (int) time;
        if (!continent.equals("Saved Capitals") && !continent.equals("Saved Flags")) {
            DataHandler dataHandler = new DataHandler(this);
            dataHandler.increaseTotalCounter(continent, gameMode);
        }
        clearProgress();
        if (win){
            showWinMessage(getResources().getString(R.string.congratulations), getResources().getString(R.string.success_this_level), getResources().getString(R.string.thanks), resultTime);
        }
        else {
            showLoseMessage(getResources().getString(R.string.no_more_lives), getResources().getString(R.string.question_lose));
        }
    }
    private void restartQuiz() {
        // Reset variables
        answeredQuestionsCount = 0;
        livesRemaining = 5;
        countryList = new ArrayList<>(countryListFull);

        // Update answersRemaining based on the continent
        if (continent.equals(getResources().getString(R.string.saved_capitals)) ||
                continent.equals(getResources().getString(R.string.saved_flags))) {
            answersRemaining = countryList.size();
        } else if (continent.equals(getResources().getString(R.string.all_countries))) {
            answersRemaining = countryList.size();
        } else if (continent.equals(getResources().getString(R.string.south_america))) {
            answersRemaining = 30;
        } else if (continent.equals(getResources().getString(R.string.oceania))) {
            answersRemaining = 30;
        } else if (continent.equals(getResources().getString(R.string.europe)) ||
                continent.equals(getResources().getString(R.string.africa)) ||
                continent.equals(getResources().getString(R.string.north_america)) ||
                continent.equals(getResources().getString(R.string.asia))) {
            answersRemaining = 60;
        }

        answeredCountriesList.clear();
        Collections.shuffle(countryList);

        // Reset UI elements
        handleImageFlags(true);

        updateLivesRemainingText();
        updateAnswersRemainingText();

        displayQuestion();
        startTime = System.currentTimeMillis();
    }


    private void showWinMessage(String title, String message, String answer, int time){
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

        // Set the onDismiss listener to handle dismissal of the dialog
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (UtilityClass.isConnectedToInternet(FlagQuizActivity.this)) {
                    DataHandler dataHandler = new DataHandler(FlagQuizActivity.this);
                    dataHandler.updateGameModeProgressAndStartActivity(continent, gameMode, time);
                }
                else {
                    showWinMessage(title, message, answer, time);
                    Toast.makeText(FlagQuizActivity.this, getResources().getString(R.string.please_connect), Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }
    private void showLoseMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(getResources().getString(R.string.educational_mode), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startEducationalMode();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.try_again), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartQuiz();
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                handleImageFlags(false);
                clearProgress();
            }
        });

        dialog.show();
    }
    private void handleImageFlags(boolean bool){
        option1Flag.setEnabled(bool);
        option2Flag.setEnabled(bool);
        option3Flag.setEnabled(bool);
        option4Flag.setEnabled(bool);
        if (bool){
            option1Flag.setAlpha(1f);
            option2Flag.setAlpha(1f);
            option3Flag.setAlpha(1f);
            option4Flag.setAlpha(1f);
        }
    }
    private void startEducationalMode(){
        educationalMode = true;
        answeredQuestionsCount = 0;
        livesRemaining = -1;
        countryList = new ArrayList<>(countryListFull);

        answeredCountriesList.clear();
        Collections.shuffle(countryList);

        // Reset UI elements
        handleImageFlags(true);

        livesRemainingTextView.setVisibility(View.INVISIBLE);
        answersRemainingTextView.setVisibility(View.INVISIBLE);

        displayQuestion();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actionBar.setTitle(continent + " (" + countryListFull.size() + ")");
        handler.post(updateTimeRunnable);
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Stop updating time when the activity is paused
        handler.removeCallbacks(updateTimeRunnable);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void saveProgress() {
        SharedPreferences sharedPreferences = getSharedPreferences(getSharedPreferencesName(String.valueOf(gameMode), continentEnglish), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save answered countries
        StringBuilder stringBuilder = new StringBuilder();
        for (Country country : answeredCountriesList) {
            stringBuilder.append(country.getName()).append(",");
        }
        editor.putString("answeredCountries", stringBuilder.toString());

        editor.putInt("livesRemaining", livesRemaining);
        editor.putInt("answersRemaining", answersRemaining);
        editor.putInt("answeredQuestionsCount", answeredQuestionsCount);
        editor.putBoolean("educationalMode", educationalMode);

        editor.apply();
    }


    private String getSharedPreferencesName(String quizType, String continent) {
        return "QuizProgress_" + quizType + "_" + continent;
    }
    private List<Country> loadProgress(int lives, int answers, int answersCount) {
        SharedPreferences sharedPreferences = getSharedPreferences(getSharedPreferencesName(String.valueOf(gameMode), continentEnglish), MODE_PRIVATE);

        String answeredCountriesString = sharedPreferences.getString("answeredCountries", "");
        String[] countryNames = answeredCountriesString.split(",");

        List<Country> answeredCountriesList = new ArrayList<>();
        for (String countryName : countryNames) {
            // Find the corresponding Country object and add it to the list
            for (Country country : countryList) {
                if (country.getName().equals(countryName)) {
                    answeredCountriesList.add(country);
                    countryList.remove(country);
                    break;
                }
            }
        }

        // Load remaining lives and answers count
        livesRemaining = sharedPreferences.getInt("livesRemaining", lives);
        answersRemaining = sharedPreferences.getInt("answersRemaining", answers);
        answeredQuestionsCount = sharedPreferences.getInt("answeredQuestionsCount", answersCount);
        updateAnswersRemainingText();
        updateLivesRemainingText();
        educationalMode = sharedPreferences.getBoolean("educationalMode", false);
        if (educationalMode){
            livesRemainingTextView.setVisibility(View.INVISIBLE);
            answersRemainingTextView.setVisibility(View.INVISIBLE);
        }
        startTime = System.currentTimeMillis() - sharedPreferences.getLong("time", 0) ;

        return answeredCountriesList;
    }


    private void clearProgress() {
        SharedPreferences sharedPreferences = getSharedPreferences(getSharedPreferencesName(String.valueOf(gameMode), continentEnglish), MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quiz_menu, menu);

        MenuItem saveItem = menu.findItem(R.id.saveIcon);
        MenuItem deleteItem = menu.findItem(R.id.deleteIcon);
        MenuItem helpItem = menu.findItem(R.id.helpIcon);
        MenuItem leaderboardItem = menu.findItem(R.id.leaderboardIcon);
        MenuItem statisticsItem = menu.findItem(R.id.statisticsIcon);
        leaderboardItem.setVisible(false);
        statisticsItem.setVisible(false);

        if (continent.equals("Saved Flags")) {
            deleteItem.setVisible(true);
            deleteItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.deleteIcon) {
                        deleteCurrentCountry();
                        return true;
                    }
                    return false;
                }
            });
        } else {
            saveItem.setVisible(true);
            saveItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.saveIcon) {
                        saveCurrentCountry();
                        return true;
                    }
                    return false;
                }
            });
            helpItem.setVisible(true);
            helpItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.helpIcon) {
                        Intent intent = new Intent(FlagQuizActivity.this, ShowHelpActivity.class);
                        intent.putExtra("activityName", "FlagQuiz");
                        startActivity(intent);
                        return true;
                    }
                    return false;
                }
            });
        }

        return true;
    }
    @Override
    public void onBackPressed() {
        if (continent.equals("Saved Flags")){
            Intent intent = new Intent(FlagQuizActivity.this, ChooseContinentOrSavedActivity.class);
            startActivity(intent);
        }
        else
            super.onBackPressed();
    }
}
