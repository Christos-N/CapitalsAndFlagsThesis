package com.unipi.chris.capitalsandflags;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminChooseActionActivity extends AppCompatActivity {
    private String selectedEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_choose_action);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.choose_action);
        actionBar.setDisplayHomeAsUpEnabled(true);

        selectedEmail = getIntent().getStringExtra("selectedUserEmail");
        TextView userEmailTextView = findViewById(R.id.userEmailTextView);
        userEmailTextView.setText(selectedEmail);
        Button viewStatsButton = findViewById(R.id.viewStatsButton);
        Button makeAdminButton = findViewById(R.id.makeAdminButton);
        Button resetProgressButton = findViewById(R.id.resetProgressButton);

        viewStatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UtilityClass.isConnectedToInternet(AdminChooseActionActivity.this)) {
                    Intent intent = new Intent(AdminChooseActionActivity.this, ShowStatsActivity.class);
                    intent.putExtra("continent", "everything");
                    intent.putExtra("emailForAdmin", selectedEmail);
                    startActivity(intent);
                }
                else
                    Toast.makeText(AdminChooseActionActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
            }
        });

        makeAdminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UtilityClass.isConnectedToInternet(AdminChooseActionActivity.this))
                    showMessage(true);
                else
                    Toast.makeText(AdminChooseActionActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
            }
        });

        resetProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UtilityClass.isConnectedToInternet(AdminChooseActionActivity.this))
                    showMessage(false);
                else
                    Toast.makeText(AdminChooseActionActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, AdminViewUsersActivity.class);
        startActivity(intent);
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
                Intent intent = new Intent(AdminChooseActionActivity.this, ShowHelpActivity.class);
                intent.putExtra("activityName", "AdminChooseAction");
                startActivity(intent);
                return true;
            }
        });
        return true;
    }
    private void showMessage(boolean makeAdmin) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message;
        if (makeAdmin)
            message = this.getResources().getString(R.string.promote_to_admin);
        else
            message = this.getResources().getString(R.string.reset_progress_question);
        builder.setTitle(selectedEmail)
                .setMessage(message)
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (UtilityClass.isConnectedToInternet(AdminChooseActionActivity.this)) {
                            if (makeAdmin)
                                makeAdmin();
                            else
                                resetProgress();
                        }
                        else
                            Toast.makeText(AdminChooseActionActivity.this, R.string.action_failed_please_connect, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void makeAdmin(){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(selectedEmail.replace(".", ","));

        // Check if the user is already an admin
        userRef.child("isAdmin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean isAdmin = dataSnapshot.getValue(Boolean.class);
                if (isAdmin != null && !isAdmin) {
                    // User is not an admin, update the value
                    userRef.child("isAdmin").setValue(true)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AdminChooseActionActivity.this, AdminChooseActionActivity.this.getResources().getString(R.string.user_is_now_an_admin), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AdminChooseActionActivity.this, AdminChooseActionActivity.this.getResources().getString(R.string.failed_user_admin), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // User is already an admin
                    Toast.makeText(AdminChooseActionActivity.this, AdminChooseActionActivity.this.getResources().getString(R.string.user_already_admin), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdminChooseActionActivity.this, AdminChooseActionActivity.this.getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void resetProgress(){
        DataHandler dataHandler = new DataHandler(this);
        dataHandler.initializeUserProgressToDatabase(selectedEmail, false);
        Toast.makeText(AdminChooseActionActivity.this, AdminChooseActionActivity.this.getResources().getString(R.string.successful_progress_reset), Toast.LENGTH_SHORT).show();
    }
}