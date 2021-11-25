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
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private Button login, singUp;
    private EditText emailField, passwordField;
    private TextInputLayout emaiLayout, passwordLayout;
    private FirebaseDatabase rootNode;
    private Utilities utilities;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        utilities = Utilities.getInstance();
        preferences = getSharedPreferences(Utilities.sharedPreferencesName, MODE_PRIVATE);
        editor = getSharedPreferences(Utilities.sharedPreferencesName, MODE_PRIVATE).edit();
        String user = preferences.getString("currentUser", null);
        System.out.println("USR1:" + user);
        if (user != null) {
            Utilities.getInstance().setCurrentUsername(user, editor);
            launchHome(getBaseContext());
        }
        try {
            /*JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(new FileReader("./prova.json"));//path to the JSON file.
            Toast t = new Toast(this.getApplicationContext());
            t.setDuration(Toast.LENGTH_LONG);
            t.setText(data.toString());
            t.show();*/
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
            JSONObject object = new JSONObject(s);
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
        FirebaseAuth auth = utilities.getAuthorisation();
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
                ProgressDialog progressDialog = new ProgressDialog(v.getContext());
                progressDialog.setMessage(Utilities.verifying);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setIndeterminate(true);
                progressDialog.show();
                passwordLayout.setError(null);
                emaiLayout.setError(null);
                String email = emailField.getText().toString();
                String password = passwordField.getText().toString();
                DatabaseReference reference = rootNode.getReference().child("users");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (email.isEmpty()) {
                            passwordLayout.setError(null);
                            emaiLayout.setError(Utilities.noUsername);
                            progressDialog.dismiss();
                            return;
                        } else if (password.isEmpty()) {
                            passwordLayout.setError(Utilities.noPassword);
                            emaiLayout.setError(null);
                            progressDialog.dismiss();
                            return;
                        }

                        auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d("TAG", "signInWithEmail:success");
                                            FirebaseUser user = utilities.getAuthorisation().getCurrentUser();
                                            editor.putString("currentUser", user.getUid());
                                            editor.apply();
                                            updateUI(user);
                                            Utilities.getInstance().setCurrentUsername(user.getUid(), editor);
                                            progressDialog.dismiss();
                                            launchHome(v.getContext());
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w("TAG", "signInWithEmail:failure", task.getException());
                                            progressDialog.dismiss();
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
        FirebaseUser currentUser = utilities.getAuthorisation().getCurrentUser();
        if (currentUser != null)
            currentUser.reload();
    }

    private void launchHome(Context context) {
        Intent intent = new Intent(context, HomeContainer.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                utilities.clear();
                finish();
                Process.killProcess(Process.myPid());
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void updateUI(FirebaseUser user) {
        user = utilities.getAuthorisation().getCurrentUser();
        /*-------- Check if user is already logged in or not--------*/
        if (user != null) {
            Toast.makeText(this.getApplicationContext(), "Login Success.",
                    Toast.LENGTH_SHORT).show();
            utilities.setCurrentUsername(user.getUid(), editor);


        }

    }
}