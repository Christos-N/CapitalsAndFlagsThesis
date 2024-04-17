package com.unipi.chris.capitalsandflags;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CountryListActivity extends AppCompatActivity {
    private ListView countryListView;
    private List<Country> countryList;
    private ArrayAdapter<Country> adapter;
    private ActionBar actionBar;
    private String selectedContinent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        countryListView = findViewById(R.id.countryListView);
        countryList = new ArrayList<>();

        selectedContinent = getIntent().getStringExtra("continent");

        countryList = (List<Country>) getIntent().getSerializableExtra("list");

        // Use the custom layout for each item
        adapter = new ArrayAdapter<Country>(this, R.layout.list_item_country, R.id.countryTextView, countryList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView countryTextView = view.findViewById(R.id.countryTextView);
                TextView capitalTextView = view.findViewById(R.id.capitalTextView);

                Country country = getItem(position);
                if (country != null) {
                    countryTextView.setText(country.getName());
                    capitalTextView.setText(country.getCapital());
                }

                return view;
            }
        };
        countryListView.setAdapter(adapter);

        // Implement sorting for Country and Capital
        countryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(selectedContinent.equals("Saved Capitals") || selectedContinent.equals("Saved Flags"))
                    showCountryDetail(position, selectedContinent);
                else
                    showCountryDetail(position, "");
            }
        });
        //First sort by country
        countryList.sort(new Comparator<Country>() {
            @Override
            public int compare(Country c1, Country c2) {
                return c1.getName().compareTo(c2.getName());
            }
        });
        adapter.notifyDataSetChanged();

        // Sort by Country when clicked on the "Country" label
        TextView countryLabel = findViewById(R.id.countryLabel);
        countryLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countryList.sort(new Comparator<Country>() {
                    @Override
                    public int compare(Country c1, Country c2) {
                        return c1.getName().compareTo(c2.getName());
                    }
                });
                adapter.notifyDataSetChanged();
            }
        });

        // Sort by Capital when clicked on the "Capital" label
        TextView capitalLabel = findViewById(R.id.capitalLabel);
        capitalLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countryList.sort(new Comparator<Country>() {
                    @Override
                    public int compare(Country c1, Country c2) {
                        return c1.getCapital().compareTo(c2.getCapital());
                    }
                });
                adapter.notifyDataSetChanged();
            }
        });
    }
    private void showCountryDetail(int position, String whichSaved) {
        Intent intent = new Intent(this, CountryDetailActivity.class);
        intent.putParcelableArrayListExtra("countryList", new ArrayList<>(countryList));
        intent.putExtra("position", position);
        intent.putExtra("saved", whichSaved);
        intent.putExtra("area", selectedContinent);
        startActivity(intent);
    }
    @Override
    protected void onResume() {
        super.onResume();
        actionBar.setTitle(selectedContinent + " (" + countryList.size() + ")");
    }
    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(this, ViewTablesActivity.class);
        startActivity(intent);
        return true;
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
                    Intent intent = new Intent(CountryListActivity.this, ShowHelpActivity.class);
                    intent.putExtra("activityName", "CountryList");
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
        return true;
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ViewTablesActivity.class);
        startActivity(intent);
    }
}