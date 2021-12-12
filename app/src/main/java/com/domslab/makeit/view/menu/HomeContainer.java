package com.domslab.makeit.view.menu;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.domslab.makeit.R;
import com.domslab.makeit.model.Utilities;
import com.domslab.makeit.view.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.domslab.makeit.view.menu.*;

import java.sql.SQLOutput;

public class HomeContainer extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView navigationView;
    private UserFragment userFragment;
    private HomeFragment homeFragment;
    private SearchFragment searchFragment;
    private AddFragment addFragment;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private DatabaseReference reference;
    private FirebaseDatabase rootNode;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("SONO MORTO");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        editor = getSharedPreferences(Utilities.sharedPreferencesName, MODE_PRIVATE).edit();
        preferences = getSharedPreferences(Utilities.sharedPreferencesName, MODE_PRIVATE);
        rootNode = FirebaseDatabase.getInstance(Utilities.path);
        reference = rootNode.getReference("users");
        loadMenuFragment();


    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.setOnNavigationItemSelectedListener(this);
        navigationView.setSelectedItemId(R.id.home);
        navigationView.getMenu().findItem(R.id.add).setVisible(preferences.getBoolean("advanced", false));

    }

    private void loadMenuFragment() {
        userFragment = new UserFragment();
        homeFragment = new HomeFragment();
        searchFragment = new SearchFragment();
        addFragment = new AddFragment();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        /*if (preferences.getInt("navbarHeight", 0) == 0 || preferences.getInt("contentHeight", 0) == 0) {
            editor.putInt("navbarHeight", navigationView.getMeasuredHeight());
            editor.putInt("contentHeight", (int) ((preferences.getInt("screenHeight", 0) - navigationView.getMeasuredHeight()) * preferences.getFloat("deviceDensity", 1.0f)));
            editor.apply();
        }*/
        switch (item.getItemId()) {
            case R.id.profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, userFragment).commit();
                return true;
            case R.id.home:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();

                return true;
            case R.id.search:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, searchFragment).commit();
                return true;
            case R.id.add:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, addFragment).commit();
                return true;
            case R.id.logout:
                try {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(HomeContainer.this);
                    builder.setMessage("Do you want to Logout?");
                    builder.setCancelable(true);
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editor.putString("currentUser", null);
                            editor.putString("advanced", null);
                            editor.putString("currentEMail", null);
                            editor.putString("currentPassword", null);
                            editor.apply();
                            Utilities.getAuthorisation().signOut();
                            Utilities.clear();
                            finish();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } finally {
                    return true;
                }
        }
        return false;
    }

/*
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = Utilities.getAuthorisation().getCurrentUser();
        if (currentUser != null)
            currentUser.reload();
    }*/


}