package com.domslab.makeit.view.menu;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.domslab.makeit.R;
import com.domslab.makeit.callback.FirebaseCallBack;
import com.domslab.makeit.model.Utilities;
import com.domslab.makeit.view.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class HomeContainer extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView navigationView;
    private UserFragment userFragment;
    private SearchFragment searchFragment;
    private AddFragment addFragment;
    private HomeFragment homeFragment;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private DatabaseReference reference;
    private FirebaseDatabase rootNode;

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        updateUI();
    }

    private void updateUI() {
        if (navigationView.getSelectedItemId() != R.id.logout) {
            FirebaseUser user = Utilities.getAuthorisation().getCurrentUser();
            if (user != null) {
                Utilities.setCurrentUsername(user.getUid());
                Query checkUser = reference.orderByChild(user.getUid());
                checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean business = false;
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot o : dataSnapshot.getChildren())
                                if (o.getKey().equals(Utilities.getCurrentUID())) {
                                    business = (boolean) o.child("advanced").getValue();
                                    editor.putBoolean(o.getKey() + "wait", (Boolean) o.child("waiting").getValue());
                                    if (preferences.getBoolean("advanced", false) != business) {
                                        editor.putBoolean("advanced", business);
                                    }
                                    editor.apply();
                                }
                            navigationView.getMenu().findItem(R.id.add).setVisible(business && !preferences.getBoolean(Utilities.getCurrentUID() + "wait", false));
                            if (navigationView.getSelectedItemId() != R.id.home)
                                navigationView.setSelectedItemId(navigationView.getSelectedItemId());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        }
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
        navigationView.setOnItemSelectedListener(this);
        navigationView.setSelectedItemId(R.id.home);
        navigationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUI();
            }
        });
    }

    private void loadMenuFragment() {
        userFragment = UserFragment.newInstance();
        searchFragment = SearchFragment.newInstance();
        addFragment = AddFragment.newInstance();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, userFragment).commitNow();
                return true;
            case R.id.home:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, HomeFragment.newInstance()).commitNow();
                return true;
            case R.id.search:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, searchFragment).commitNow();
                return true;
            case R.id.add:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, addFragment).commitNow();
                return true;
            case R.id.logout:
                try {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(HomeContainer.this, R.style.MyAlertDialogTheme);
                    builder.setTitle("Attenzione!");
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