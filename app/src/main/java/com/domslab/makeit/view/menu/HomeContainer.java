package com.domslab.makeit.view.menu;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.domslab.makeit.R;
import com.domslab.makeit.model.Utilities;
import com.domslab.makeit.view.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


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
    //private MeowBottomNavigation bottomNavigation;

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        editor = getSharedPreferences(Utilities.sharedPreferencesName, MODE_PRIVATE).edit();
        preferences = getSharedPreferences(Utilities.sharedPreferencesName, MODE_PRIVATE);
        rootNode = FirebaseDatabase.getInstance(Utilities.path);
        reference = rootNode.getReference("users");

        /*bottomNavigation = (MeowBottomNavigation) findViewById(R.id.bottomNavigationView);
        bottomNavigation.setCountTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.vollkorn_variablefont_wght));
        bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.icons8_user_100px));
        bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.icons8_search_100px));
        bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.icons8_home_52px));
        if (preferences.getBoolean("advanced", false))
            bottomNavigation.add(new MeowBottomNavigation.Model(4, R.drawable.icons8_add_60px));
        bottomNavigation.add(new MeowBottomNavigation.Model(5, R.drawable.icons8_logout_rounded_left_64px));
       */ loadMenuFragment();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.setOnNavigationItemSelectedListener(this);
        navigationView.setOnItemSelectedListener(this);
        navigationView.setSelectedItemId(R.id.home);
        navigationView.getMenu().findItem(R.id.add).setVisible(preferences.getBoolean("advanced", false));


        /*for (MeowBottomNavigation.Model model : bottomNavigation.getModels())
            System.out.println(model.getId());
        System.out.println(bottomNavigation.getChildCount());
       */
        /*bottomNavigation.setOnClickMenuListener(new MeowBottomNavigation.ClickListener() {
            @Override
            public void onClickItem(MeowBottomNavigation.Model item) {
                System.out.println(item.getId());
            }
        });
        bottomNavigation.setOnShowListener(new MeowBottomNavigation.ShowListener() {
            @Override
            public void onShowItem(MeowBottomNavigation.Model item) {
                System.out.println(item.getId());
                switch (item.getId()) {
                    case 1:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, userFragment).commit();
                        return;

                    case 2:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, searchFragment).commit();
                        return;
                    case 3:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
                        return;
                    case 4:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, addFragment).commit();
                        return;
                    case 5:

                        final AlertDialog.Builder builder = new AlertDialog.Builder(HomeContainer.this);
                        builder.setMessage("Vuoi disconnetterti?");
                        builder.setCancelable(true);
                        builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
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
                        return;
                }
            }
        });
        bottomNavigation.setOnReselectListener(new MeowBottomNavigation.ReselectListener() {
            @Override
            public void onReselectItem(MeowBottomNavigation.Model item) {

            }
        });
        bottomNavigation.show(3, false);*/
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
                    builder.setMessage("Vuoi disconnetterti?");
                    builder.setCancelable(true);
                    builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
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

}