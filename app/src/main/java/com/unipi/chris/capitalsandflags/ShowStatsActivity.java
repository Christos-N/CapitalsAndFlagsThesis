package com.unipi.chris.capitalsandflags;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ShowStatsActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private String emailForAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_stats);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        String cont = getIntent().getStringExtra("continent");
        emailForAdmin = getIntent().getStringExtra("emailForAdmin");

        displayGameModeStatistics(cont);
    }
    @Override
    protected void onResume() {
        super.onResume();
        actionBar.setTitle(getResources().getString(R.string.statistics));
    }
    public void displayGameModeStatistics(String cont) {
        String userEmail;
        if (emailForAdmin == null)
            userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        else
            userEmail = emailForAdmin.replace(".", ",");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userEmail);

        TextView textView = findViewById(R.id.scrollableTextView);
        String[] continents;
        if (cont.equals("everything") || cont.equals("Saved Capitals") || cont.equals("Saved Flags")) {
            continents = new String[]{"All Countries", "Europe", "Africa", "North America", "South America", "Asia", "Oceania"};
        } else {
            continents = new String[]{cont};
        }
        for (String continent : continents) {
            String continentKey = continent.toLowerCase();

            for (int mode = 1; mode <= 4; mode++) {
                final int currentMode = mode;

                String key = continentKey + "_mode" + mode;

                // Fetch completed count for the current key from "CompletedCounter"
                userRef.child("CompletedCounter").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot completedSnapshot) {
                        if (completedSnapshot.exists()) {
                            Integer completedCount = completedSnapshot.getValue(Integer.class);

                            // Fetch uncompleted count for the same key from "TotalCounter"
                            userRef.child("TotalCounter").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot totalSnapshot) {
                                    if (totalSnapshot.exists()) {
                                        Integer totalCount = totalSnapshot.getValue(Integer.class);

                                        double percentage = 0;
                                        if (totalCount != null && totalCount != 0 && completedCount != null)
                                            percentage = (double) completedCount / totalCount * 100;

                                        // Fetch and display the best time for the current key from "BestTime"
                                        double finalPercentage = percentage;
                                        userRef.child("BestTime").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot bestTimeSnapshot) {
                                                String displayText;
                                                if (bestTimeSnapshot.exists()) {
                                                    Integer bestTimeSeconds = bestTimeSnapshot.getValue(Integer.class);

                                                    // Convert seconds to minutes and seconds
                                                    int minutes = bestTimeSeconds / 60;
                                                    int seconds = bestTimeSeconds % 60;

                                                    displayText = String.format("%s %d: %d/%d %.2f%% %02d:%02d", continent, currentMode, completedCount, totalCount, finalPercentage, minutes, seconds);
                                                } else {
                                                    displayText = String.format("%s %d: %d/%d %.2f%%", continent, currentMode, completedCount, totalCount, finalPercentage);
                                                }
                                                textView.append(displayText + "\n");
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError bestTimeError) {
                                                Toast.makeText(ShowStatsActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError uncompletedError) {
                                    Toast.makeText(ShowStatsActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError completedError) {
                        Toast.makeText(ShowStatsActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    public void onBackPressed() {
        if (emailForAdmin != null){
            Intent intent = new Intent(this, AdminChooseActionActivity.class);
            intent.putExtra("selectedUserEmail", emailForAdmin);
            startActivity(intent);
        }
        else
            super.onBackPressed();
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
                    Intent intent = new Intent(ShowStatsActivity.this, ShowHelpActivity.class);
                    intent.putExtra("activityName", "ShowStats");
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
        return true;
    }
}