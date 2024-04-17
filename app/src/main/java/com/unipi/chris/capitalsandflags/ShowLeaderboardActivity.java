package com.unipi.chris.capitalsandflags;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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

import java.util.ArrayList;
import java.util.List;

public class ShowLeaderboardActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private TextView textView;
    private String continent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_leaderboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        textView = findViewById(R.id.scrollableTextView);

        continent = getIntent().getStringExtra("continent");
        displayLeaderboard(continent);
    }
    private void displayLeaderboard(String continent) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        List<UserScore> leaderboard = new ArrayList<>();

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    int totalScore = 0;
                    String email = userSnapshot.getKey();
                    DataSnapshot modeSnapshot;
                    if (continent.equals("everything") || continent.equals("Saved Capitals") || continent.equals("Saved Flags")) {
                        modeSnapshot = userSnapshot.child("CompletedCounter");
                        for (DataSnapshot snapshot : modeSnapshot.getChildren()) {
                            Integer modeScore = snapshot.getValue(Integer.class);
                            if (modeScore != null) {
                                totalScore += modeScore;
                            }
                        }
                    }
                    else {
                        for (int mode = 1; mode <= 4; mode++) {
                            String key = continent.toLowerCase() + "_mode" + mode;
                            modeSnapshot = userSnapshot.child("CompletedCounter").child(key);
                            if (modeSnapshot.exists()) {
                                Integer modeScore = modeSnapshot.getValue(Integer.class);
                                if (modeScore != null) {
                                    totalScore += modeScore;
                                }
                            }
                        }
                    }

                    leaderboard.add(new UserScore(email, totalScore));
                }

                // Sort the leaderboard based on total score
                leaderboard.sort((user1, user2) -> Integer.compare(user2.getTotalScore(), user1.getTotalScore()));

                // Now leaderboard contains the sorted list of users based on their total scores for the given continent
                displayLeaderboardData(leaderboard);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ShowLeaderboardActivity.this, R.string.error, Toast.LENGTH_SHORT).show();

            }
        });
    }

    // Method to display the leaderboard data
    private void displayLeaderboardData(List<UserScore> leaderboard) {
        for (UserScore userScore : leaderboard) {
            if (userScore.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){   // Make user's data red
                SpannableString spannableString = new SpannableString(getString(R.string.score) + userScore.getTotalScore() + getString(R.string.leaderboard_email) + userScore.getEmail() + "\n\n");
                spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.append(spannableString);
            }
            else
                textView.append(getString(R.string.score) + userScore.getTotalScore() + getString(R.string.leaderboard_email) + userScore.getEmail() + "\n\n");
        }
    }

    // Class to store user ID and total score
    private static class UserScore {
        private String email;
        private int totalScore;

        public UserScore(String email, int totalScore) {
            this.email = email;
            this.totalScore = totalScore;
        }

        public String getEmail() {
            return email.replace(",",".");
        }

        public int getTotalScore() {
            return totalScore;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (continent.equals("everything") || continent.equals("Saved Capitals") || continent.equals("Saved Flags"))
            actionBar.setTitle(getResources().getString(R.string.leaderboard));
        else
            actionBar.setTitle(continent);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    public void onBackPressed() {
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
                    Intent intent = new Intent(ShowLeaderboardActivity.this, ShowHelpActivity.class);
                    intent.putExtra("activityName", "ShowLeaderboard");
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
        return true;
    }
}
