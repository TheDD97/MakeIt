package com.domslab.makeit.view;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Process;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.domslab.makeit.callback.FirebaseCallBack;
import com.domslab.makeit.R;
import com.domslab.makeit.model.Utilities;
import com.domslab.makeit.view.menu.HomeContainer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Button login, singUp;
    private EditText emailField, passwordField;
    private TextInputLayout emailLayout, passwordLayout;
    private TextView resetPassword;
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
        if (user != null && psw != null && email != null) {
            Utilities.setCurrentUsername(user);
            Utilities.showProgressDialog(this);
            auth.signInWithEmailAndPassword(email, psw)
                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = Utilities.getAuthorisation().getCurrentUser();
                                updateUI(user, new FirebaseCallBack() {
                                    @Override
                                    public void onCallBack(List<String> list, boolean business, boolean wait) {
                                        Toast.makeText(MainActivity.this, "Login Riuscito.",
                                                Toast.LENGTH_SHORT).show();
                                        launchHome(getApplicationContext());
                                        finish();
                                    }
                                });
                            } else {
                                Utilities.closeProgressDialog();
                                Toast.makeText(MainActivity.this, "Accesso fallito.",
                                        Toast.LENGTH_SHORT).show();

                                updateUI(null, new FirebaseCallBack() {
                                    @Override
                                    public void onCallBack(List<String> list, boolean business, boolean wait) {

                                    }
                                });
                            }
                        }
                    });
        }
        rootNode = FirebaseDatabase.getInstance(Utilities.path);
        reference = rootNode.getReference().child("users");
        emailField = findViewById(R.id.login_email);
        passwordField = findViewById(R.id.login_password);
        emailLayout = findViewById(R.id.email_layout);
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
                Utilities.showProgressDialog(v.getContext());
                passwordLayout.setError(null);
                emailLayout.setError(null);
                String email = emailField.getText().toString().trim().toLowerCase();
                String password = passwordField.getText().toString();
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (email.isEmpty()) {
                            passwordLayout.setError(null);
                            emailLayout.setError(Utilities.noUsername);
                            Utilities.closeProgressDialog();
                            return;
                        } else if (password.isEmpty()) {
                            passwordLayout.setError(Utilities.noPassword);
                            emailLayout.setError(null);
                            Utilities.closeProgressDialog();
                            return;
                        }
                        auth.signInWithEmailAndPassword(email.trim().toLowerCase(Locale.ROOT), password)
                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        Utilities.showProgressDialog(getApplicationContext());
                                        if (task.isSuccessful()) {
                                            FirebaseUser user = Utilities.getAuthorisation().getCurrentUser();
                                            editor.putString("currentUser", user.getUid());
                                            editor.putString("currentEmail", email);
                                            editor.putString("currentPassword", password);
                                            editor.apply();
                                            updateUI(user, new FirebaseCallBack() {
                                                @Override
                                                public void onCallBack(List<String> list, boolean business, boolean wait) {
                                                    Toast.makeText(MainActivity.this, "Login Riuscito.",
                                                            Toast.LENGTH_SHORT).show();
                                                    launchHome(getApplicationContext());
                                                    finish();
                                                }
                                            });
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Utilities.closeProgressDialog();
                                            Toast.makeText(v.getContext().getApplicationContext(), "Accesso fallito.",
                                                    Toast.LENGTH_SHORT).show();
                                            updateUI(null, new FirebaseCallBack() {
                                                @Override
                                                public void onCallBack(List<String> list, boolean business, boolean wait) {

                                                }
                                            });
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Utilities.closeProgressDialog();
                    }
                });
            }
        });
        resetPassword = findViewById(R.id.reset_password);
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });
    }

    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
        builder.setTitle("Reimposta Password");
        LinearLayout linearLayout = new LinearLayout(this);
        final EditText emailet = new EditText(this);
        emailet.setHint("Email");
        emailet.setMinEms(16);
        emailet.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        linearLayout.addView(emailet);
        linearLayout.setPadding(10, 10, 10, 10);
        builder.setView(linearLayout);
        builder.setPositiveButton("Reimposta", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailet.getText().toString().trim();
                if (email.isEmpty())
                    Toast.makeText(MainActivity.this, "Inserisci l'email prima di procedere", Toast.LENGTH_LONG).show();
                else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                    Toast.makeText(MainActivity.this, "Formato email errato", Toast.LENGTH_LONG).show();
                else
                    beginRecovery(email);
            }
        });
        builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void beginRecovery(String email) {
        ProgressDialog loadingBar = new ProgressDialog(this);
        loadingBar.setMessage("Sto inviando....");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingBar.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Invio Completato", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Qualcosa ?? andato storto", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(MainActivity.this, "Errore", Toast.LENGTH_LONG).show();
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
        Intent intent = new Intent(context, HomeContainer.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
        builder.setMessage("Vuoi chiudere l'applicazione?");
        builder.setCancelable(true);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
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


    private void updateUI(FirebaseUser user, FirebaseCallBack callBack) {
        user = Utilities.getAuthorisation().getCurrentUser();
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
                                editor.putBoolean("advanced", business);
                                boolean previousState = preferences.getBoolean(o.getKey() + "wait", false);
                                boolean currentState = (boolean) o.child("waiting").getValue();
                                boolean previousBusinessState = preferences.getBoolean(o.getKey() + "business", false);
                                if ((previousState && !currentState && business) || (!previousBusinessState && business && !currentState))
                                    notifyUpdate(true);
                                else if ((previousState != currentState && !currentState && !business) || (previousBusinessState && !business && !currentState))
                                    notifyUpdate(false);
                                editor.putBoolean(o.getKey() + "wait", currentState);
                                editor.putBoolean(o.getKey() + "business", business);
                                editor.apply();
                                callBack.onCallBack(null, false, false);
                            }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void notifyUpdate(boolean advanced) {
        String contentTitle;
        String contentText;
        String description = "Account Upgrade";
        if (advanced) {
            contentTitle = "Congratulazioni!";
            contentText = "Hai ottenuto un account Aziendale!";
        } else {
            contentTitle = "Brutte notizie";
            contentText = "Il tuo account ?? stato degradato!";
        }
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationChannel channel = new NotificationChannel("makeIt", "Upgrade", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(description);
        NotificationManager notificationManager = this.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel.getId())
                .setSmallIcon(R.drawable.ic_news)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSound(alarmSound)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(100, builder.build());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utilities.closeProgressDialog();
    }
}