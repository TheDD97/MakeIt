package com.domslab.makeit.view.menu;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.domslab.makeit.FirebaseCallBack;
import com.domslab.makeit.R;
import com.domslab.makeit.model.UserHelperClass;
import com.domslab.makeit.model.Utilities;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserFragment extends Fragment {

    private TextView usernameLabel;
    private EditText name, surname, email, nEmail, username, password, confirmPassword;
    private TextInputLayout nameLayout, surnameLayout, emailLayout, nEmaiLayout, usernameLayout, passwordLayout, confirmPasswordLayout;
    private static UserHelperClass user = null;
    private boolean editing = false, check = true;
    private ArrayList<TextInputLayout> layouts;
    private ArrayList<EditText> texts;
    private boolean noPassword;
    private boolean noEmail;
    private ScrollView scrollView;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private DatabaseReference reference;
    private FirebaseDatabase rootNode;

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = Utilities.getAuthorisation().getCurrentUser();
        if (currentUser != null) {
            currentUser.reload();
        }
    }

    public static UserFragment newInstance() {
        UserFragment fragment = new UserFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getActivity().getSharedPreferences(Utilities.sharedPreferencesName, Context.MODE_PRIVATE);
        editor = getActivity().getSharedPreferences(Utilities.sharedPreferencesName, Context.MODE_PRIVATE).edit();
        Utilities.showProgressDialog(getContext(), true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scrollView = view.findViewById(R.id.user_fragment_scroll);
        System.out.println("content height" + preferences.getInt("contentHeight", 0));
        //scrollView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, preferences.getInt("contentHeight", 0)));

        layouts = new ArrayList<>();
        texts = new ArrayList<>();
        usernameLabel = view.findViewById(R.id.username_label);
        name = view.findViewById(R.id.name);
        nameLayout = view.findViewById(R.id.name_layout);
        surname = view.findViewById(R.id.surname);
        surnameLayout = view.findViewById(R.id.surname_layout);
        email = view.findViewById(R.id.email);
        emailLayout = view.findViewById(R.id.email_layout);
        nEmail = view.findViewById(R.id.new_email);
        username = view.findViewById(R.id.username);
        usernameLayout = view.findViewById(R.id.username_layout);
        nEmaiLayout = view.findViewById(R.id.new_email_layout);
        /*passwordLayout = view.findViewById(R.id.password_layout);
        password = view.findViewById(R.id.password);
        confirmPasswordLayout = view.findViewById(R.id.confirm_password_layout);
        confirmPassword = view.findViewById(R.id.confirm_password);
        */
        layouts.addAll(Arrays.asList(usernameLayout, nameLayout, surnameLayout, emailLayout, nEmaiLayout));
        texts.addAll(Arrays.asList(username, name, surname, email, nEmail));
        disableAll();
        Button edit = view.findViewById(R.id.edit);
        Button cancel = view.findViewById(R.id.cancel);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.showProgressDialog(v.getContext(), false);
                clearError(layouts.size());
                if (!editing) {
                    enableAll();
                    editing = true;
                    edit.setText("Confirm");
                    cancel.setBackgroundColor(Color.RED);
                } else {
                    checkUsername();
                    checkName();
                    checkSurname();
                    checkEmail();
                    //checkPassword();
                    if (check) {
                        //user = utilities.currentUser;
                        Toast t = new Toast(v.getContext());
                        t.setDuration(Toast.LENGTH_LONG);
                        FirebaseUser firebaseUser = Utilities.getAuthorisation().getCurrentUser();
                        UserHelperClass userUpdate = null;
                        if (!noEmail) {
                            if (firebaseUser.getEmail().equals(email.getText().toString().trim())) {
                                firebaseUser.updateEmail(nEmail.getText().toString().trim());
                                userUpdate = new UserHelperClass(name.getText().toString(), surname.getText().toString(), nEmail.getText().toString(), user.getAdvanced(), username.getText().toString(), user.getWaiting());
                            }
                        } else
                            userUpdate = new UserHelperClass(name.getText().toString(), surname.getText().toString(), preferences.getString("currentEmail", null), user.getAdvanced(), username.getText().toString(), user.getWaiting());
                        updateUser(userUpdate, t);
                        editing = false;
                        cancel.setBackgroundColor(Color.BLUE);
                        edit.setText("Edit");
                    }
                    check = true;
                }
                Utilities.closeProgressDialog();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editing) {
                    disableAll();
                    cancel.setBackgroundColor(Color.BLUE);
                    editing = false;
                    edit.setText("edit");
                    clearError(layouts.size());
                    fillField();
                }
            }
        });

        Log.d("USER", "CREATED");
    }

    private void updateUser(UserHelperClass userUpdate, Toast t) {
        rootNode = FirebaseDatabase.getInstance(Utilities.path);
        reference = rootNode.getReference("users");
        reference.child(Utilities.getAuthorisation().getUid()).setValue(userUpdate);
        user = userUpdate;
        usernameLabel.setText("Hi, " + user.getUsername() + "!");
        t.setText("Done");
        t.show();
        disableAll();
        editing = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("USRFRG", "ONPAUSE");
        setCurrentUser();
    }

    private void setCurrentUser() {
        rootNode = FirebaseDatabase.getInstance(Utilities.path);
        reference = rootNode.getReference("users");
        readUserData(new FirebaseCallBack() {
            @Override
            public void onCallBack(List<String> list, boolean business, boolean wait) {
                name.setText(list.get(0));
                surname.setText(list.get(1));
                username.setText(list.get(2));
                usernameLabel.setText("Hi " + list.get(2) + "!");
                user = new UserHelperClass(list.get(0), list.get(1), preferences.getString("currentEmail", null), business, list.get(2), wait);
                Utilities.closeProgressDialog();
            }
        });
    }

    private void readUserData(FirebaseCallBack callBack) {
        Query checkUser = reference.orderByChild(Utilities.getCurrentUID());
        ArrayList<String> userData = new ArrayList<>();
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = "", surname = "", username = "";
                boolean business = false, wait = false;
                UserHelperClass userHelperClass = null;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot o : dataSnapshot.getChildren())
                        if (o.getKey().equals(Utilities.getCurrentUID())) {
                            name = o.child("name").getValue().toString();
                            surname = o.child("surname").getValue().toString();
                            business = (boolean) o.child("advanced").getValue();
                            username = o.child("username").getValue().toString();
                            wait = (boolean) o.child("waiting").getValue();
                            userData.addAll(Arrays.asList(name, surname, username));
                            callBack.onCallBack(userData, business, wait);
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fillField() {
        name.setText(user.getName());
        surname.setText(user.getSurname());
        username.setText(user.getUsername());
    }

    private void checkUsername() {
        if (check) {
            if (username.getText().toString().equals("") || username.getText().toString().isEmpty()) {
                check = false;
                usernameLayout.setError(Utilities.signUpEmptyUsername);
            }
        }
    }

    private void checkName() {
        if (check) {
            clearError(1);
            if (name.getText().toString().equals("") || name.getText().toString().isEmpty()) {
                check = false;
                nameLayout.setError(Utilities.signUpNameError);
            }
        }
    }

    private void checkSurname() {
        if (check) {
            clearError(2);
            if (surname.getText().toString().equals("")) {
                check = false;
                surnameLayout.setError(Utilities.signUpSurnameError);
            }
        }
    }

    private void checkEmail() {
        if (check) {
            clearError(3);
            noEmail = false;
            if (email.getText().toString().isEmpty() || email.getText() == null) {
                noEmail = true;
            } else if (!email.getText().toString().equals(preferences.getString("currentEmail", null))) {
                emailLayout.setError(Utilities.emailNoMatch);
                check = false;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
                check = false;
                emailLayout.setError(Utilities.signUpWrongEmailFormat);
            } else if (!Patterns.EMAIL_ADDRESS.matcher(nEmail.getText().toString()).matches()) {
                check = false;
                nEmaiLayout.setError(Utilities.signUpWrongEmailFormat);
            } else if ((email.getText().toString().isEmpty() || email.getText() == null) && (!nEmail.getText().toString().isEmpty() || nEmail.getText() != null)) {
                check = false;
                nEmaiLayout.setError(Utilities.noOldEmail);
            }
        }
    }

    private void disableAll() {
        for (EditText t : texts) {
            t.setFocusable(false);
            t.setFocusableInTouchMode(false);
            t.setClickable(false);
            t.setCursorVisible(false);
        }

    }

    private void enableAll() {
        for (EditText t : texts) {
            t.setFocusable(true);
            t.setFocusableInTouchMode(true);
            t.setClickable(true);
            t.setCursorVisible(true);
        }
    }

    private void clearError(int numField) {
        for (int i = 0; i < layouts.size() && i < numField; i++)
            layouts.get(i).setError(null);
    }

}