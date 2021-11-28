package com.domslab.makeit.menu;

import android.app.ProgressDialog;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.domslab.makeit.FirebaseCallBack;
import com.domslab.makeit.R;
import com.domslab.makeit.UserHelperClass;
import com.domslab.makeit.Utilities;
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
import java.util.regex.Pattern;

public class UserFragment extends Fragment {

    private TextView usernameLabel;
    private EditText name, surname, email, username, password, confirmPassword;
    private TextInputLayout nameLayout, surnameLayout, emailLayout, usernameLayout, passwordLayout, confirmPasswordLayout;
    private static UserHelperClass user = null;
    private boolean editing = false, check = true;
    private ArrayList<TextInputLayout> layouts;
    private ArrayList<EditText> texts;
    private boolean noPassword;
    private ScrollView scrollView;
    private SharedPreferences preferences;
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
        username = view.findViewById(R.id.username);
        usernameLayout = view.findViewById(R.id.username_layout);
        passwordLayout = view.findViewById(R.id.password_layout);
        password = view.findViewById(R.id.password);
        confirmPasswordLayout = view.findViewById(R.id.confirm_password_layout);
        confirmPassword = view.findViewById(R.id.confirm_password);
        layouts.addAll(Arrays.asList(nameLayout, surnameLayout, emailLayout, usernameLayout, passwordLayout, confirmPasswordLayout));
        texts.addAll(Arrays.asList(name, surname, email, username, password, confirmPassword));
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
                    checkName();
                    checkSurname();
                    checkEmail();
                    checkUsername();
                    checkPassword();
                    if (check) {
                        //user = utilities.currentUser;
                        Toast t = new Toast(v.getContext());
                        t.setDuration(Toast.LENGTH_LONG);
                        FirebaseUser firebaseUser = Utilities.getAuthorisation().getCurrentUser();
                        if (!firebaseUser.getEmail().equals(email.getText().toString().trim()))
                            firebaseUser.updateEmail(email.getText().toString().trim());
                        if (!noPassword)
                            firebaseUser.updatePassword(password.getText().toString().trim());
                        UserHelperClass userUpdate = new UserHelperClass(name.getText().toString(), surname.getText().toString(), email.getText().toString(), user.getAdvanced(), username.getText().toString());
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
        Utilities.currentUser = userUpdate;
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
        //usernameLabel.setText(DBManager.getInstance().getCurrentUser().getUsername());
        /*
        usernameLabel.setText("Hi, " + utilities.currentUser.getUsername() + "!");
        fillField();
*/

        setCurrentUser();
    }

    private void setCurrentUser() {
        rootNode = FirebaseDatabase.getInstance(Utilities.path);
        reference = rootNode.getReference("users");
        readUserData(new FirebaseCallBack() {
            @Override
            public void onCallBack(List<String> list, boolean business) {
                name.setText(list.get(0));
                surname.setText(list.get(1));
                email.setText(list.get(2));
                username.setText(list.get(3));
                usernameLabel.setText("Hi " + list.get(3) + "!");
                user = new UserHelperClass(list.get(0), list.get(1), list.get(2), business, list.get(3));
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
                String name = "", surname = "", email = "", username = "";
                boolean business = false;
                UserHelperClass userHelperClass = null;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot o : dataSnapshot.getChildren())
                        if (o.getKey().equals(Utilities.getCurrentUID())) {
                            name = o.child("name").getValue().toString();
                            surname = o.child("surname").getValue().toString();
                            email = o.child("email").getValue().toString();
                            business = (boolean) o.child("advanced").getValue();
                            username = o.child("username").getValue().toString();
                            userData.addAll(Arrays.asList(name, surname, email, username));
                            callBack.onCallBack(userData, business);
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
        email.setText(user.getEmail());
        username.setText(user.getUsername());
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
            if (username.getText().toString().equals("") || username.getText().toString().isEmpty()) {
                check = false;
                usernameLayout.setError(Utilities.signUpEmptyUsername);
            }
        }
    }

    private void checkEmail() {
        if (check) {
            clearError(2);
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
            clearError(1);
            if (surname.getText().toString().equals("")) {
                check = false;
                surnameLayout.setError(Utilities.signUpSurnameError);
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

    private void checkPassword() {

        if (check) {
            noPassword = false;
            clearError(texts.size());
            if (password.getText().toString().equals(confirmPassword.getText().toString()) && password.getText().toString().isEmpty()) {
                noPassword = true;
                return;
            }
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
}