package com.unipi.chris.capitalsandflags;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity {
    private List<Country> countryList, countryListFull;
    private int currentQuestionIndex, livesRemaining, answersRemaining;
    private int answeredQuestionsCount = 0;
    private Button option1Button, option2Button, option3Button, option4Button;
    private TextView selectedCountryTextView, livesRemainingTextView, answersRemainingTextView;
    private ImageView flagImageView;
    private ActionBar actionBar;
    private String continent, capitalOrCountry;
    private List<Country> answeredCountriesList = new ArrayList<>();
    private boolean educationalMode = false;
    private long startTime;
    private Handler handler;
    private Runnable updateTimeRunnable;

    private int gameMode;   //1: Capital by Country, 2: Country By Capital, 3: Country By Flag (4: Flag By Country)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        selectedCountryTextView = findViewById(R.id.selectedCountryTextView);
        flagImageView = findViewById(R.id.flagImageView);
        option1Button = findViewById(R.id.option1Button);
        option2Button = findViewById(R.id.option2Button);
        option3Button = findViewById(R.id.option3Button);
        option4Button = findViewById(R.id.option4Button);
        livesRemainingTextView = findViewById(R.id.livesRemainingTextView);
        answersRemainingTextView = findViewById(R.id.answersRemainingTextView);

        if (educationalMode){
            livesRemainingTextView.setVisibility(View.INVISIBLE);
            answersRemainingTextView.setVisibility(View.INVISIBLE);
        }

        capitalOrCountry = getIntent().getStringExtra("capitalOrCountry");
        continent = getIntent().getStringExtra("continent");

        List<Country> receivedList = (List<Country>) getIntent().getSerializableExtra("list");
        countryList = receivedList;
        countryListFull = new ArrayList<>(countryList);
        answersRemaining = countryList.size();

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

        if (capitalOrCountry.equals("guessCapital"))
            gameMode = 1;
        else if (capitalOrCountry.equals("guessCountry"))
            gameMode = 2;
        else
            gameMode = 3;
        updateAnswersRemainingText();

        currentQuestionIndex = 0;
        livesRemaining = 5;
        updateLivesRemainingText();

        if (getIntent().getBooleanExtra("load", false) &&
                getSharedPreferences(getSharedPreferencesName(String.valueOf(gameMode), continent), MODE_PRIVATE)
                        .getInt("livesRemaining", 5) != 0 ) {
            loadProgress(livesRemaining, answersRemaining, answeredQuestionsCount);
            displayQuestion(true);
        }
        else {
            Collections.shuffle(countryList);
            clearProgress();
            displayQuestion(false);
            startTime = System.currentTimeMillis();
        }


        option1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(option1Button.getText().toString());
            }
        });

        option2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(option2Button.getText().toString());
            }
        });

        option3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(option3Button.getText().toString());
            }
        });

        option4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(option4Button.getText().toString());
            }
        });
        handleButtons(true);
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
        SharedPreferences sharedPreferences = getSharedPreferences(getSharedPreferencesName(String.valueOf(gameMode), continent), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("time", elapsedTime);
        editor.apply();
    }
    private void saveCurrentCountry(String tableName){
        if (currentQuestionIndex >= 0 && currentQuestionIndex < countryList.size()) {
            Country currentCountry = countryList.get(currentQuestionIndex);

            DataHandler dataHandler = new DataHandler(this);
            dataHandler.saveCountry(currentCountry.getFlagImageName(), tableName);
        }
    }
    private void deleteCurrentCountry(String tableName){
        if (currentQuestionIndex >= 0 && currentQuestionIndex < countryList.size() && UtilityClass.isConnectedToInternet(this)) {
            Country currentCountry = countryList.get(currentQuestionIndex);

            DataHandler dataHandler = new DataHandler(this);
            dataHandler.deleteCountry(currentCountry.getFlagImageName(), tableName);
        }
    }
    private void displayQuestion(boolean load) {
        Country currentCountry = countryList.get(currentQuestionIndex);

        if (!load) {
            List<String> options = new ArrayList<>();
            switch (gameMode) {
                case 1:
                    options.add(currentCountry.getCapital());
                    break;
                case 2:
                case 3:
                    options.add(currentCountry.getName());
                    break;
            }

            // Add three other random as options
            List<Country> randomCountries = getRandomCountries(currentCountry, 3);
            for (Country country : randomCountries) {
                switch (gameMode) {
                    case 1:
                        options.add(country.getCapital());
                        break;
                    case 2:
                    case 3:
                        options.add(country.getName());
                        break;
                }
            }


            Collections.shuffle(options);

            option1Button.setText(options.get(0));
            option2Button.setText(options.get(1));
            option3Button.setText(options.get(2));
            option4Button.setText(options.get(3));
        }

        switch (gameMode){
            case 1:
                selectedCountryTextView.setText(currentCountry.getName());
                selectedCountryTextView.setVisibility(View.VISIBLE);
                break;
            case 2:
                selectedCountryTextView.setText(currentCountry.getCapital());
                selectedCountryTextView.setVisibility(View.VISIBLE);
                break;
        }
        displayImageFromLocalStorage(currentCountry.getFlagImageName(), flagImageView);
    }
    private void displayImageFromLocalStorage(String imageName, ImageView imageView) {
        File localFile;
        if (gameMode == 2)
            localFile = new File(getFilesDir(), imageName+"2.jpg");
        else
            localFile = new File(getFilesDir(), imageName+".png");

        if (localFile.exists()) {
            // If the file exists locally, load it into an ImageView
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        }
    }
    private List<Country> getRandomCountries(Country excludeCountry, int count) {
        List<Country> randomCountries = new ArrayList<>(countryListFull);
        randomCountries.remove(excludeCountry);
        outer: if (gameMode == 1){
            for (Country country: randomCountries){
                if (country.getFlagImageName().equals("jamaica") || country.getFlagImageName().equals("norfolk_island")){   //Jamaica and Norfolk island have "Kingston" as capitals
                    randomCountries.remove(country);
                    break outer;
                }
            }
        }
        Collections.shuffle(randomCountries);
        return randomCountries.subList(0, count);
    }
    private void checkAnswer(String selectedAnswer) {
        if (UtilityClass.isConnectedToInternet(this)) {
            Country currentCountry = countryList.get(currentQuestionIndex);
            String correctAnswer;
            if (gameMode == 1)
                correctAnswer = currentCountry.getCapital();
            else
                correctAnswer = currentCountry.getName();
            if ((gameMode == 1 && selectedAnswer.equals(currentCountry.getCapital())) ||
                    (gameMode == 2 && selectedAnswer.equals(currentCountry.getName())) ||
                    (gameMode == 3 && selectedAnswer.equals(currentCountry.getName()))) {
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
                if (option1Button.getText().toString().equals(selectedAnswer)) {
                    option1Button.setEnabled(false);
                    option1Button.setBackgroundColor(Color.RED);
                    option1Button.setTextColor(Color.WHITE); // Set text color to white
                } else if (option2Button.getText().toString().equals(selectedAnswer)) {
                    option2Button.setEnabled(false);
                    option2Button.setBackgroundColor(Color.RED);
                    option2Button.setTextColor(Color.WHITE);
                } else if (option3Button.getText().toString().equals(selectedAnswer)) {
                    option3Button.setEnabled(false);
                    option3Button.setBackgroundColor(Color.RED);
                    option3Button.setTextColor(Color.WHITE);
                } else if (option4Button.getText().toString().equals(selectedAnswer)) {
                    option4Button.setEnabled(false);
                    option4Button.setBackgroundColor(Color.RED);
                    option4Button.setTextColor(Color.WHITE);
                }

                if (livesRemaining == 0) {
                    // Out of lives, end the quiz
                    if (option1Button.getText().toString().equals(correctAnswer)) {
                        option1Button.setBackgroundColor(Color.GREEN);
                    } else if (option2Button.getText().toString().equals(correctAnswer)) {
                        option2Button.setBackgroundColor(Color.GREEN);
                    } else if (option3Button.getText().toString().equals(correctAnswer)) {
                        option3Button.setBackgroundColor(Color.GREEN);
                    } else if (option4Button.getText().toString().equals(correctAnswer)) {
                        option4Button.setBackgroundColor(Color.GREEN);
                    }
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
        handleButtons(true);

        if (answersRemaining == 0){
            endQuiz(true);
        }
        else
            displayQuestion(false);
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
        handleButtons(true);

        updateLivesRemainingText();
        updateAnswersRemainingText();

        displayQuestion(false);
        startTime = System.currentTimeMillis();
    }
    private void showWinMessage(String title, String message, String answer, int time){
        // Create an instance of AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the title and message of the dialog
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false);

        // Add a button to the dialog
        builder.setPositiveButton(answer, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();

        // Set the onDismiss listener to handle dismissal of the dialog
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (UtilityClass.isConnectedToInternet(QuizActivity.this)) {
                    DataHandler dataHandler = new DataHandler(QuizActivity.this);
                    dataHandler.updateGameModeProgressAndStartActivity(continent, gameMode, time);
                }
                else {
                    showWinMessage(title, message, answer, time);
                    Toast.makeText(QuizActivity.this, getResources().getString(R.string.please_connect), Toast.LENGTH_SHORT).show();
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
                handleButtons(false);
            }
        });

        dialog.show();
    }
    private void handleButtons(boolean bool){
        option1Button.setEnabled(bool);
        option2Button.setEnabled(bool);
        option3Button.setEnabled(bool);
        option4Button.setEnabled(bool);
        if (bool){
            option1Button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
            option2Button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
            option3Button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
            option4Button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
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
        handleButtons(true);

        livesRemainingTextView.setVisibility(View.INVISIBLE);
        answersRemainingTextView.setVisibility(View.INVISIBLE);

        displayQuestion(false);
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
        SharedPreferences sharedPreferences = getSharedPreferences(getSharedPreferencesName(String.valueOf(gameMode), continent), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save answered countries
        StringBuilder stringBuilder = new StringBuilder();
        for (Country country : answeredCountriesList) {
            stringBuilder.append(country.getName()).append(",");
        }
        editor.putString("answeredCountries", stringBuilder.toString());
        // Save countryList
        stringBuilder = new StringBuilder();
        for (Country country : countryList) {
            stringBuilder.append(country.getName()).append(",");
        }
        editor.putString("countryList", stringBuilder.toString());

        editor.putInt("livesRemaining", livesRemaining);
        editor.putInt("answersRemaining", answersRemaining);
        editor.putInt("answeredQuestionsCount", answeredQuestionsCount);
        editor.putBoolean("educationalMode", educationalMode);

        // Save current question index
        editor.putInt("currentQuestionIndex", currentQuestionIndex);

        // Save displayed options
        editor.putString("option1", option1Button.getText().toString());
        editor.putString("option2", option2Button.getText().toString());
        editor.putString("option3", option3Button.getText().toString());
        editor.putString("option4", option4Button.getText().toString());

        editor.apply();
    }



    private String getSharedPreferencesName(String quizType, String continent) {
        return "QuizProgress_" + quizType + "_" + continent;
    }
    private void loadProgress(int lives, int answers, int answersCount) {
        SharedPreferences sharedPreferences = getSharedPreferences(getSharedPreferencesName(String.valueOf(gameMode), continent), MODE_PRIVATE);

        String answeredCountriesString = sharedPreferences.getString("answeredCountries", "");
        String[] countryNames = answeredCountriesString.split(",");

        List<Country> answeredCountries = new ArrayList<>();
        for (String countryName : countryNames) {
            // Find the corresponding Country object and add it to the list
            for (Country country : countryList) {
                if (country.getName().equals(countryName)) {
                    answeredCountries.add(country);
                    countryList.remove(country);
                    break;
                }
            }
        }

        answeredCountriesList = new ArrayList<>(answeredCountries);

        // Load countryList
        String countryListString = sharedPreferences.getString("countryList", "");
        String[] countryListNames = countryListString.split(",");
        List<Country> loadedCountryList = new ArrayList<>();
        for (String countryName : countryListNames) {
            // Find the corresponding Country object and add it to the list
            for (Country country : countryListFull) {
                if (country.getName().equals(countryName)) {
                    loadedCountryList.add(country);
                    break;
                }
            }
        }
        countryList = new ArrayList<>(loadedCountryList);

        // Load remaining lives and answers count
        livesRemaining = sharedPreferences.getInt("livesRemaining", lives);
        answersRemaining = sharedPreferences.getInt("answersRemaining", answers);
        answeredQuestionsCount = sharedPreferences.getInt("answeredQuestionsCount", answersCount);
        updateAnswersRemainingText();
        updateLivesRemainingText();
        educationalMode = sharedPreferences.getBoolean("educationalMode", false);
        if (educationalMode) {
            livesRemainingTextView.setVisibility(View.INVISIBLE);
            answersRemainingTextView.setVisibility(View.INVISIBLE);
        }

        // Load displayed options
        String option1 = sharedPreferences.getString("option1", "");
        String option2 = sharedPreferences.getString("option2", "");
        String option3 = sharedPreferences.getString("option3", "");
        String option4 = sharedPreferences.getString("option4", "");

        // Set the loaded options to the option buttons
        option1Button.setText(option1);
        option2Button.setText(option2);
        option3Button.setText(option3);
        option4Button.setText(option4);

        startTime = System.currentTimeMillis() - sharedPreferences.getLong("time", 0) ;
    }



    private void clearProgress() {
        SharedPreferences sharedPreferences = getSharedPreferences(getSharedPreferencesName(String.valueOf(gameMode), continent), MODE_PRIVATE);
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

        if (continent.equals("Saved Capitals")||
                continent.equals("Saved Flags")) {
            deleteItem.setVisible(true);
            deleteItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (UtilityClass.isConnectedToInternet(QuizActivity.this)) {
                        if (continent.equals("Saved Capitals"))
                            deleteCurrentCountry("Saved Capitals");
                        else
                            deleteCurrentCountry("Saved Flags");
                    }
                    else
                        Toast.makeText(QuizActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        } else {
            saveItem.setVisible(true);
            saveItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (UtilityClass.isConnectedToInternet(QuizActivity.this)) {
                        if (continent.equals("Saved Capitals") || gameMode <= 2)
                            saveCurrentCountry("Saved Capitals");
                        else
                            saveCurrentCountry("Saved Flags");
                    }
                    else
                        Toast.makeText(QuizActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
        helpItem.setVisible(true);
        helpItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(QuizActivity.this, ShowHelpActivity.class);
                intent.putExtra("activityName", "Quiz");
                startActivity(intent);
                return true;
            }
        });
        return true;
    }
    @Override
    public void onBackPressed() {
        if (continent.equals("Saved Capitals")||
                continent.equals("Saved Flags")){
            Intent intent = new Intent(QuizActivity.this, ChooseContinentOrSavedActivity.class);
            startActivity(intent);
        }
        else
            super.onBackPressed();
    }
}
