package com.domslab.makeit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;

import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.HashMap;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {
    private EditText name, surname, username, email, password, confirmPassword;
    private TextInputLayout nameLayout, surnameLayout, usernameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private Button confirm, cancel;
    private HashMap<String, Object> user;
    private RadioButton yes, no;
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    SharedPreferences.Editor editor;
    private Boolean check = true;
    private Toast t;
    private Utilities utilities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        editor = getSharedPreferences(Utilities.sharedPreferencesName, MODE_PRIVATE).edit();
        rootNode = FirebaseDatabase.getInstance(Utilities.path);
        reference = rootNode.getReference().child("users");
        user = new HashMap<>();
        name = findViewById(R.id.sign_up_name);
        nameLayout = findViewById(R.id.name_layout);
        surname = findViewById(R.id.sign_up_surname);
        surnameLayout = findViewById(R.id.surname_layout);
        username = findViewById(R.id.sign_up_username);
        usernameLayout = findViewById(R.id.username_layout);
        email = findViewById(R.id.sign_up_email);
        emailLayout = findViewById(R.id.email_layout);
        password = findViewById(R.id.sign_up_password);
        passwordLayout = findViewById(R.id.password_layout);
        confirmPassword = findViewById(R.id.sign_up_confirm_password);
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout);
        yes = findViewById(R.id.yesRadio);
        no = findViewById(R.id.noRadio);
        no.setChecked(true);
        confirm = findViewById(R.id.confirm_sign_up);
        cancel = findViewById(R.id.cancel_sing_up);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkName();
                checkSurname();
                checkUsername();
                checkEmail();
                checkPassword();
                utilities = Utilities.getInstance();
                if (check) {
                    clearAllError();
                    FirebaseAuth mAuth = utilities.getAuthorisation();
                    mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("TAG", "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        updateUI(user);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(v.getContext(), "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        updateUI(null);
                                    }
                                }
                            });
                    finish();
                }

                check = true;
            }
        });
    }

    private void clearAllError() {
        nameLayout.setError(null);
        surnameLayout.setError(null);
        usernameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);
    }

    private void checkPassword() {

        if (check) {
            nameLayout.setError(null);
            surnameLayout.setError(null);
            usernameLayout.setError(null);
            emailLayout.setError(null);
            if (password.getText().toString().isEmpty() || password.getText() == null) {
                check = false;
                passwordLayout.setError(Utilities.noPassword);

            } else if (confirmPassword.getText().toString().isEmpty() || confirmPassword.getText() == null) {
                check = false;
                confirmPasswordLayout.setError(Utilities.noConfirmPassword);
            } else if (!password.getText().toString().equals(confirmPassword.getText().toString())) {
                check = false;
                passwordLayout.setError(Utilities.noBothPsw);
                confirmPasswordLayout.setError(Utilities.noBothPsw);
            } else if (!Pattern.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>\\.]).{8,20}$",
                    password.getText())) {
                check = false;
                passwordLayout.setError(Utilities.passwordFormat);
            }
        }

    }

    private void checkEmail() {
        if (check) {
            nameLayout.setError(null);
            surnameLayout.setError(null);
            usernameLayout.setError(null);
            if (email.getText().toString().isEmpty() || email.getText() == null) {
                check = false;
                emailLayout.setError(Utilities.signUpEmptyEmail);
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
                check = false;
                emailLayout.setError(Utilities.signUpWrongEmailFormat);
            }
        }
    }

    private void checkSurname() {
        if (check) {
            nameLayout.setError(null);
            if (surname.getText().toString().equals("")) {
                check = false;
                surnameLayout.setError(Utilities.signUpSurnameError);
            }
        }
    }

    private void checkName() {
        if (check) {
            if (name.getText().toString().equals("") || name.getText().toString().isEmpty()) {
                check = false;
                nameLayout.setError(Utilities.signUpNameError);
            }
        }
    }

    private void checkUsername() {
        if (check) {
            nameLayout.setError(null);
            surnameLayout.setError(null);
            if (!username.getText().toString().equals("")) {
                if (user.containsKey(username.getText().toString())) {
                    usernameLayout.setError(Utilities.signUpUsernameAlreadyExists);
                    check = false;
                }

            } else {
                usernameLayout.setError(Utilities.signUpEmptyUsername);
                check = false;
            }
        }
    }

    private void updateUI(FirebaseUser user) {
        user = utilities.getAuthorisation().getCurrentUser();
        if (user == null) {
            Toast.makeText(this.getApplicationContext(), "ERROR",
                    Toast.LENGTH_SHORT).show();
        } else {
            String id = utilities.getAuthorisation().getCurrentUser().getUid();
            System.out.println(id);
            UserHelperClass nUser = new UserHelperClass(name.getText().toString(), surname.getText().toString(), email.getText().toString(), password.getText().toString(), yes.isChecked(), username.getText().toString());
            reference.child(id).setValue(nUser);
        }
    }
}


