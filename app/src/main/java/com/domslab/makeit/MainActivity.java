package com.domslab.makeit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.domslab.makeit.menu.HomeContainer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private Button login, singUp;
    private EditText emailField, passwordField;
    private TextInputLayout emaiLayout, passwordLayout;
    private FirebaseDatabase rootNode;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseAuth auth = Utilities.getAuthorisation();
        preferences = getSharedPreferences(Utilities.sharedPreferencesName, MODE_PRIVATE);
        editor = getSharedPreferences(Utilities.sharedPreferencesName, MODE_PRIVATE).edit();
        String user = preferences.getString("currentUser", null);
        String email = preferences.getString("currentEmail", null);
        String psw = preferences.getString("currentPassword", null);
        System.out.println(psw);

        if (user != null && psw != null && email != null) {
            Utilities.setCurrentUsername(user);
            auth.signInWithEmailAndPassword(email, psw)
                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("TAG", "signInWithEmail:success");
                                FirebaseUser user = Utilities.getAuthorisation().getCurrentUser();
                                editor.putString("currentUser", user.getUid());
                                editor.putString("currentEmail", email);
                                editor.putString("currentPassword", psw);
                                editor.apply();
                                updateUI(user);
                                Utilities.setCurrentUsername(user.getUid());
                                finish();
                                launchHome(getApplicationContext());
                            } else {
                                updateUI(null);
                            }
                        }
                    });
            launchHome(getBaseContext());
        }
        try {
            /*JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(new FileReader("./prova.json"));//path to the JSON file.
            Toast t = new Toast(this.getApplicationContext());
            t.setDuration(Toast.LENGTH_LONG);
            t.setText(data.toString());
            t.show();
            String s = "{\n" +
                    "  \"1\":[\n" +
                    "    {\n" +
                    "      \"text\":\"ciao\",\n" +
                    "      \"image\":\"154793\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"2\":[\n" +
                    "    {\n" +
                    "      \"text\":\"no\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
            JSONObject object = new JSONObject(s);*/
            //System.out.println("result" + object.getJSONArray("1").getJSONObject(0).has("ciao"));
        } catch (Exception e) {
            e.printStackTrace();

            /*String string = jsonObject.getJSONObject("1").getString("text");*/

        }
        rootNode = FirebaseDatabase.getInstance(Utilities.path);
        emailField = findViewById(R.id.login_email);
        passwordField = findViewById(R.id.login_password);
        emaiLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        login = findViewById(R.id.login);
        singUp = findViewById(R.id.sign_up_login_page);
        singUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SignUp.class);
                startActivity(intent);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.showProgressDialog(v.getContext(), false);
                passwordLayout.setError(null);
                emaiLayout.setError(null);
                String email = emailField.getText().toString();
                String password = passwordField.getText().toString();
                reference = rootNode.getReference().child("users");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (email.isEmpty()) {
                            passwordLayout.setError(null);
                            emaiLayout.setError(Utilities.noUsername);
                            Utilities.closeProgressDialog();
                            return;
                        } else if (password.isEmpty()) {
                            passwordLayout.setError(Utilities.noPassword);
                            emaiLayout.setError(null);
                            Utilities.closeProgressDialog();
                            return;
                        }
                        auth.signInWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d("TAG", "signInWithEmail:success");
                                            FirebaseUser user = Utilities.getAuthorisation().getCurrentUser();
                                            editor.putString("currentUser", user.getUid());
                                            editor.putString("currentEmail", email);
                                            editor.putString("currentPassword", password);
                                            editor.apply();
                                            Utilities.closeProgressDialog();
                                            updateUI(user);
                                            finish();
                                            launchHome(v.getContext());
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Utilities.closeProgressDialog();
                                            Log.w("TAG", "signInWithEmail:failure", task.getException());
                                            Toast.makeText(v.getContext().getApplicationContext(), "Authentication failed.",
                                                    Toast.LENGTH_SHORT).show();
                                            updateUI(null);
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = Utilities.getAuthorisation().getCurrentUser();
        if (currentUser != null)
            currentUser.reload();
    }

    private void launchHome(Context context) {
        if (preferences.getFloat("deviceDensity", 0.0f) == 0.0f) {
            editor.putFloat("deviceDensity", getResources().getDisplayMetrics().density);
            editor.putInt("screenWidth", getResources().getDisplayMetrics().widthPixels);
            editor.putInt("screenHeight", getResources().getDisplayMetrics().heightPixels);
            editor.apply();
        }
        Intent intent = new Intent(context, HomeContainer.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to Exit?");
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
                Utilities.clear();
                finish();
                Process.killProcess(Process.myPid());
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void updateUI(FirebaseUser user) {
        user = Utilities.getAuthorisation().getCurrentUser();
        /*-------- Check if user is already logged in or not--------*/
        if (user != null) {
            Toast.makeText(this.getApplicationContext(), "Login Success.",
                    Toast.LENGTH_SHORT).show();
            Utilities.setCurrentUsername(user.getUid());
            readUserData();
        }

    }

    private void readUserData() {
        Query checkUser = reference.orderByChild(Utilities.getCurrentUID());
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean business = false;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot o : dataSnapshot.getChildren())
                        if (o.getKey().equals(Utilities.getCurrentUID())) {
                            business = (boolean) o.child("advanced").getValue();
                            editor.putBoolean("advanced", business);
                            editor.apply();
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}