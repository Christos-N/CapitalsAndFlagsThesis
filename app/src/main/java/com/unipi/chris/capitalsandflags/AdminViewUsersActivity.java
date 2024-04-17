package com.unipi.chris.capitalsandflags;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminViewUsersActivity extends AppCompatActivity {

    private List<String> userEmailList;
    private FirebaseAuth mAuth;

    private ArrayAdapter<String> userListAdapter;
    private boolean doubleBackToExitPressedOnce = false;
    private static final int BACK_PRESS_INTERVAL = 3000; // 3 seconds
    private List<String> originalUserEmailList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_users);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.choose_user);

        mAuth = FirebaseAuth.getInstance();
        ListView userListListView = findViewById(R.id.userListListView);
        Button switchButton = findViewById(R.id.switchButton);
        Button logoutButton = findViewById(R.id.logoutButton);
        userEmailList = new ArrayList<>();
        userListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userEmailList);
        userListListView.setAdapter(userListAdapter);
        EditText searchEditText = findViewById(R.id.searchEditText);
        originalUserEmailList = new ArrayList<>();

        displayUserEmails();

        userListListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedUserEmail = userEmailList.get(position);
                openAdminChooseActionActivity(selectedUserEmail);
            }
        });
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UtilityClass.isConnectedToInternet(AdminViewUsersActivity.this)) {
                    Toast.makeText(AdminViewUsersActivity.this, R.string.switched_to_player, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AdminViewUsersActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                else
                    Toast.makeText(AdminViewUsersActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
            }
        });
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutMessage();
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Filter the original list based on the search input
                filterUserEmails(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }
    private void filterUserEmails(String searchText) {
        userEmailList.clear();

        if (TextUtils.isEmpty(searchText)) {
            // If the search text is empty, show the original list
            userEmailList.addAll(originalUserEmailList);
        } else {
            // Filter the original list based on the search input
            for (String userEmail : originalUserEmailList) {
                if (userEmail.contains(searchText)) {
                    userEmailList.add(userEmail);
                }
            }
        }

        // Notify the adapter of the changes
        userListAdapter.notifyDataSetChanged();
    }

    private void showLogoutMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.logout)
                .setMessage(R.string.question_logout)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logoutUser();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(AdminViewUsersActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    private void displayUserEmails() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                originalUserEmailList.clear();
                userEmailList.clear();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userEmail = userSnapshot.getKey().replace(",",".");  //decode email
                    originalUserEmailList.add(userEmail);
                    userEmailList.add(userEmail);
                }

                userListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdminViewUsersActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void openAdminChooseActionActivity(String selectedUserEmail) {
        Intent intent = new Intent(this, AdminChooseActionActivity.class);
        intent.putExtra("selectedUserEmail", selectedUserEmail);
        startActivity(intent);
    }
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finishAffinity(); // Close app
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getResources().getString(R.string.back_again), Toast.LENGTH_SHORT).show();

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, BACK_PRESS_INTERVAL);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
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
                    Intent intent = new Intent(AdminViewUsersActivity.this, ShowHelpActivity.class);
                    intent.putExtra("activityName", "AdminViewUsers");
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
        return true;
    }
}
