package com.unipi.chris.capitalsandflags;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.List;

public class CountryDetailActivity extends AppCompatActivity {

    private Button nextButton, previousButton;
    private TextView countryNameTextView, capitalTextView, continentTextView;
    private ImageView flagImageView;
    private List<Country> countryList;
    private int currentPosition;
    ActionBar actionBar;
    private String selectedContinent;
    private boolean isImageToggle = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);
        countryNameTextView = findViewById(R.id.countryNameTextView);
        capitalTextView = findViewById(R.id.capitalTextView);
        continentTextView = findViewById(R.id.continentTextView);
        flagImageView = findViewById(R.id.flagImageView);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isImageToggle = false;
                if (currentPosition < countryList.size() - 1) {
                    currentPosition++;
                    showCountryDetails(currentPosition);
                } else {
                    Toast.makeText(CountryDetailActivity.this, getResources().getString(R.string.no_more_countries), Toast.LENGTH_SHORT).show();
                }
            }
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isImageToggle = false;
                if (currentPosition > 0) {
                    currentPosition--;
                    showCountryDetails(currentPosition);
                } else {
                    Toast.makeText(CountryDetailActivity.this, getResources().getString(R.string.no_more_countries), Toast.LENGTH_SHORT).show();
                }
            }
        });

        flagImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleImage(); // Toggle the image when ImageView is clicked
            }
        });

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("countryList") && intent.hasExtra("position")) {
            countryList = intent.getParcelableArrayListExtra("countryList");
            currentPosition = intent.getIntExtra("position", 0);
            selectedContinent = intent.getStringExtra("area");
            showCountryDetails(currentPosition);
        }
    }
    private void toggleImage() {
        if (isImageToggle) {
            displayImageFromLocalStorage(countryList.get(currentPosition).getFlagImageName() + ".png", flagImageView);
        } else {
            displayImageFromLocalStorage(countryList.get(currentPosition).getFlagImageName() + "2.jpg", flagImageView);
        }
        isImageToggle = !isImageToggle;
    }

    private void showCountryDetails(int position) {
        Country country = countryList.get(position);
        countryNameTextView.setText(country.getName());
        String capitalText = getString(R.string.capital_label) + country.getCapital();
        capitalTextView.setText(capitalText);
        String continentText = getString(R.string.continent_label) + country.getContinent();
        continentTextView.setText(continentText);
        displayImageFromLocalStorage(country.getFlagImageName() + ".png", flagImageView);
    }
    private void displayImageFromLocalStorage(String fullImageName, ImageView imageView) {
        File localFile = new File(getFilesDir(), fullImageName);

        if (localFile.exists()) {
            // If the file exists locally, load it into an ImageView
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        actionBar.setTitle(getResources().getString(R.string.info));
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if(selectedContinent.equals("Saved Capitals")){
            DataHandler dataHandler = new DataHandler(CountryDetailActivity.this);
            dataHandler.startActivityWithSavedCountryData("Saved Capitals", false, "", false);
        }
        else if(selectedContinent.equals("Saved Flags")){
            DataHandler dataHandler = new DataHandler(CountryDetailActivity.this);
            dataHandler.startActivityWithSavedCountryData("Saved Flags", false, "", false);
        }
        else
            super.onBackPressed();
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

        if (getIntent().getStringExtra("saved").equals("Saved Capitals")||
                getIntent().getStringExtra("saved").equals("Saved Flags")) {
            deleteItem.setVisible(true);
            deleteItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.deleteIcon) {
                        if (getIntent().getStringExtra("saved").equals("Saved Capitals"))
                            deleteCurrentCountry("Saved Capitals");
                        else
                            deleteCurrentCountry("Saved Flags");
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
                        showMessage();
                        return true;
                    }
                    return false;
                }
            });
        }
        helpItem.setVisible(true);
        helpItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.helpIcon) {
                    Intent intent = new Intent(CountryDetailActivity.this, ShowHelpActivity.class);
                    intent.putExtra("activityName", "CountryDetail");
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        return true;
    }

    public void saveCurrentCountry(String tableName){
        if (currentPosition >= 0 && currentPosition < countryList.size()) {
            Country currentCountry = countryList.get(currentPosition);

            DataHandler dataHandler = new DataHandler(this);
            dataHandler.saveCountry(currentCountry.getFlagImageName(), tableName);
        }
    }
    public void deleteCurrentCountry(String tableName){
        if (currentPosition >= 0 && currentPosition < countryList.size()) {
            Country currentCountry = countryList.get(currentPosition);

            DataHandler dataHandler = new DataHandler(this);
            dataHandler.deleteCountry(currentCountry.getFlagImageName(), tableName);
        }
    }
    private void showMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getResources().getString(R.string.save))
                .setMessage(getResources().getString(R.string.where_to_save))
                .setPositiveButton(getResources().getString(R.string.saved_capitals), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveCurrentCountry("Saved Capitals");
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.saved_flags), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveCurrentCountry("Saved Flags");
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}