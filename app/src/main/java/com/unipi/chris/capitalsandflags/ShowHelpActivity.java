package com.unipi.chris.capitalsandflags;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.TextView;

public class ShowHelpActivity extends AppCompatActivity {

    TextView textView;
    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_help);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        textView = findViewById(R.id.scrollableTextView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        actionBar.setTitle(getResources().getString(R.string.help));
        show(getIntent().getStringExtra("activityName"));
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void show(String activityName){
        switch (activityName){
            case "ChooseContinentOrSaved":
                textView.setText(getResources().getString(R.string.help_choose_continent_or_saved));
                break;
            case "ChooseQuiz":
                textView.setText(getResources().getString(R.string.help_choose_quiz));
                break;
            case "CountryDetail":
                textView.setText(getResources().getString(R.string.help_country_detail));
                break;
            case "CountryList":
                textView.setText(getResources().getString(R.string.help_country_list));
                break;
            case "FlagQuiz":
                textView.setText(getResources().getString(R.string.help_flag_quiz));
                break;
            case "Main":
                textView.setText(getResources().getString(R.string.help_main));
                break;
            case "Quiz":
                textView.setText(getResources().getString(R.string.help_quiz));
                break;
            case "ViewTables":
                textView.setText(getResources().getString(R.string.help_view_tables));
                break;
            case "Login":
                textView.setText(getResources().getString(R.string.help_login));
                break;
            case "ShowLeaderboard":
                textView.setText(getResources().getString(R.string.help_show_leaderboard));
                break;
            case "ShowStats":
                textView.setText(getResources().getString(R.string.help_show_stats));
                break;
            case "Signup":
                textView.setText(getResources().getString(R.string.help_signup));
                break;
            case "AdminViewUsers":
                textView.setText(getResources().getString(R.string.help_admin_view_users));
                break;
            case "AdminChooseAction":
                textView.setText(getResources().getString(R.string.help_admin_choose_action));
                break;
        }
    }
}