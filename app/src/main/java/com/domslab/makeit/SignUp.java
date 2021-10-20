package com.domslab.makeit;

import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {
    private EditText name, surname, username, email, password, confirmPassword;
    private Button confirm, cancel;
    private HashMap<String, Object> user;
    private RadioButton yes, no;
    private FirebaseDatabase rootNode;
    private Boolean check = true;
    private Toast t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        rootNode = FirebaseDatabase.getInstance(Utilities.path);
        user = new HashMap<>();
        name = findViewById(R.id.sign_up_name);
        surname = findViewById(R.id.sign_up_surname);
        username = findViewById(R.id.sign_up_username);
        email = findViewById(R.id.sign_up_email);
        password = findViewById(R.id.sign_up_password);
        confirmPassword = findViewById(R.id.sign_up_confirm_password);
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
        DatabaseReference reference = rootNode.getReference().child("user");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot o : dataSnapshot.getChildren()) {
                    user.put(o.getKey(), o);
                    //    System.out.println(o.getKey() + " --> " + o.getValue());
                }
                System.out.println("MAP");
                for (String i : user.keySet()) {
                    System.out.println(i + " --> " + ((DataSnapshot) user.get(i)).child("email").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t = new Toast(v.getContext());
                t.setDuration(Toast.LENGTH_SHORT);
                checkName();
                checkSurname();
                checkUsername();
                checkEmail();
                checkPassword();
                if (!check)
                    t.show();
                else {
                    UserHelperClass user = new UserHelperClass(name.getText().toString(), surname.getText().toString(), email.getText().toString(), password.getText().toString(), yes.isChecked());
                    reference.child(username.getText().toString()).setValue(user);
                }

                check = true;
            }
        });
    }

    private void checkPassword() {
        if (check) {
            if (password.getText().toString().isEmpty() || password.getText() == null) {
                check = false;
                t.setText(Utilities.noPassword);
            } else if (confirmPassword.getText().toString().isEmpty() || confirmPassword.getText() == null) {
                check = false;
                t.setText(Utilities.noConfirmPassword);
            } else if (!password.getText().toString().equals(confirmPassword.getText().toString())) {
                check = false;
                t.setText(Utilities.noBothPsw);
            } else if (!Pattern.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>\\.]).{8,20}$",
                    password.getText())) {
                check = false;
                t.setText(Utilities.passwordFormat);
            }
        }

    }

    private void checkEmail() {
        if (check) {
            if (email.getText().toString().isEmpty() || email.getText() == null) {
                check = false;
                t.setText("campo email vuoto");
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
                check = false;
                t.setText("Email non valida");
            }

        }
    }


    private void checkSurname() {
        if (check)
            if (surname.getText().toString().equals("")) {
                check = false;
                t.setText("Controlla il cognome");
            }

    }

    private void checkName() {
        if (check) {
            if (name.getText().toString().equals("")) {
                check = false;
                t.setText("Controlla il nome");
            }
        }
    }

    private void checkUsername() {
        if (check)
            if (!username.getText().toString().equals("")) {
                if (user.containsKey(username.getText().toString())) {
                    t.setText("username già esistente");
                    check = false;
                }

            } else {
                t.setText("Controlla l'username");
                check = false;
            }
    }
}
