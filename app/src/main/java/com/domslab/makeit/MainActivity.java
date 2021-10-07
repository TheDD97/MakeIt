package com.domslab.makeit;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Button login, singUp;
    private EditText usernameField, passwordField;
    private FirebaseDatabase rootNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootNode = FirebaseDatabase.getInstance(Utilities.path);
        usernameField = findViewById(R.id.login_username);
        passwordField = findViewById(R.id.login_password);
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
                String username = usernameField.getText().toString();
                String password = passwordField.getText().toString();
                DatabaseReference reference = rootNode.getReference().child("user");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Toast t = new Toast(v.getContext());
                        t.setDuration(Toast.LENGTH_SHORT);
                        if (username.isEmpty()) {
                            t.setText(Utilities.noUsername);
                            t.show();
                            return;
                        } else if (password.isEmpty()) {
                            t.setText(Utilities.noPassword);
                            t.show();
                            return;
                        }
                        for (DataSnapshot o : dataSnapshot.getChildren())
                            if (o.getKey().equals(username)) {
                                if (!o.child("password").getValue().toString().equals(password)) {
                                    t.setText(Utilities.loginError);
                                    t.show();
                                    return;
                                }
                                Intent intent = new Intent(v.getContext(), Home.class);
                                startActivity(intent);
                            } else {
                                t.setText(Utilities.noUser);
                                t.show();
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {

    }
}