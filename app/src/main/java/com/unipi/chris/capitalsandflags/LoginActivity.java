package com.unipi.chris.capitalsandflags;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextLoginEmail;
    private EditText editTextLoginPassword;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        firebaseAuth = FirebaseAuth.getInstance();
        editTextLoginEmail = findViewById(R.id.editTextLoginEmail);
        editTextLoginPassword = findViewById(R.id.editTextLoginPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonSignUpRedirect = findViewById(R.id.buttonSignUpRedirect);

        if (firebaseAuth.getCurrentUser() != null){
            checkIfAdmin(getString(R.string.welcome_back));
            toolbar.setVisibility(View.INVISIBLE);
            editTextLoginEmail.setVisibility(View.INVISIBLE);
            editTextLoginPassword.setVisibility(View.INVISIBLE);
            buttonLogin.setVisibility(View.INVISIBLE);
            buttonSignUpRedirect.setVisibility(View.INVISIBLE);
            ImageView imageView = findViewById(R.id.imageViewLogo);
            imageView.setVisibility(View.INVISIBLE);
            TextView textViewView = findViewById(R.id.textViewNoAccount);
            textViewView.setVisibility(View.INVISIBLE);
            if (!UtilityClass.isConnectedToInternet(this))
                Toast.makeText(this, R.string.please_connect, Toast.LENGTH_SHORT).show();
        }

        buttonSignUpRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                finish();
            }
        });
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UtilityClass.isConnectedToInternet(LoginActivity.this))
                    loginUser();
                else
                    Toast.makeText(LoginActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
            }
        });
        DataHandler dataHandler = new DataHandler(this);
        dataHandler.checkImagesExistInLocalStorage("flags", false);
        dataHandler.checkImagesExistInLocalStorage("places", false);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quiz_menu, menu);
        MenuItem statisticsItem = menu.findItem(R.id.statisticsIcon);
        MenuItem leaderboardItem = menu.findItem(R.id.leaderboardIcon);
        MenuItem helpItem = menu.findItem(R.id.helpIcon);
        leaderboardItem.setVisible(false);
        statisticsItem.setVisible(false);

        helpItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(LoginActivity.this, ShowHelpActivity.class);
                intent.putExtra("activityName", "Login");
                startActivity(intent);
                return true;
            }
        });
        return true;
    }

    private void loginUser() {
        String email = editTextLoginEmail.getText().toString();
        String password = editTextLoginPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, getResources().getString(R.string.please_enter_email_and_password), Toast.LENGTH_SHORT).show();
            return;
        }


        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            checkIfAdmin(getString(R.string.login_successful));
                        } else {
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void successfulPlayerLogin(String toastText){
        Toast.makeText(LoginActivity.this, toastText, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void successfulAdminLogin(String toastText){
        Toast.makeText(LoginActivity.this, toastText, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, AdminViewUsersActivity.class);
        startActivity(intent);
    }
    private void showMessage(String toastText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.login)
                .setMessage(R.string.player_or_admin)
                .setPositiveButton(getResources().getString(R.string.admin), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        successfulAdminLogin(toastText);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.player), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        successfulPlayerLogin(toastText);
                    }
                })
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    public void checkIfAdmin(String toastText) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userEmail);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // dataSnapshot contains the data of the user
                        Boolean isAdmin = dataSnapshot.child("isAdmin").getValue(Boolean.class);
                        if (isAdmin != null && isAdmin) {
                            // User is an admin
                            showMessage(toastText);
                        } else {
                            // User is not an admin
                            successfulPlayerLogin(toastText);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(LoginActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
