package com.unipi.chris.capitalsandflags;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ViewTablesActivity extends AppCompatActivity {
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tables);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Button showCountryListButton = findViewById(R.id.showCountryListButton);
        Button showEuropeButton = findViewById(R.id.showEuropeButton);
        Button showNorthAmericaButton = findViewById(R.id.showNorthAmericaButton);
        Button showSouthAmericaButton = findViewById(R.id.showSouthAmericaButton);
        Button showAfricaButton = findViewById(R.id.showAfricaButton);
        Button showAsiaButton = findViewById(R.id.showAsiaButton);
        Button showOceaniaButton = findViewById(R.id.showOceaniaButton);
        Button savedCapitalsButton = findViewById(R.id.savedCapitals);
        Button savedFlagsButton = findViewById(R.id.savedFlags);

        showCountryListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCountries("All countries");
            }
        });
        showEuropeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCountries("Europe");
            }
        });

        showNorthAmericaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCountries("North America");
            }
        });

        showSouthAmericaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCountries("South America");
            }
        });

        showAfricaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCountries("Africa");
            }
        });

        showAsiaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCountries("Asia");
            }
        });

        showOceaniaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCountries("Oceania");
            }
        });
        savedCapitalsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataHandler dataHandler = new DataHandler(ViewTablesActivity.this);
                dataHandler.startActivityWithSavedCountryData("Saved Capitals", true, "", false);
            }
        });
        savedFlagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataHandler dataHandler = new DataHandler(ViewTablesActivity.this);
                dataHandler.startActivityWithSavedCountryData("Saved Flags", true, "", false);
            }
        });
    }

    private void loadCountries(String continent) {
        if (UtilityClass.isConnectedToInternet(this)) {
            DataHandler dataHandler = new DataHandler(this);
            dataHandler.startActivityWithCountryData(continent, "", false);
        }
        else
            Toast.makeText(this, R.string.please_connect, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        actionBar.setTitle(getResources().getString(R.string.view_tables));
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quiz_menu, menu);
        MenuItem helpItem = menu.findItem(R.id.helpIcon);
        MenuItem leaderboardItem = menu.findItem(R.id.leaderboardIcon);
        MenuItem statisticsItem = menu.findItem(R.id.statisticsIcon);
        leaderboardItem.setVisible(false);
        statisticsItem.setVisible(false);
        helpItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.helpIcon) {
                    Intent intent = new Intent(ViewTablesActivity.this, ShowHelpActivity.class);
                    intent.putExtra("activityName", "ViewTables");
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
        return true;
    }
}
