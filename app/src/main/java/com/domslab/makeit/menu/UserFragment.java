package com.domslab.makeit.menu;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.domslab.makeit.R;
import com.domslab.makeit.UserHelperClass;
import com.domslab.makeit.Utilities;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TextView username;
    private EditText name, surname, email, password, confirmPassword;
    private TextInputLayout nameLayout, surnameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private UserHelperClass user;
    private boolean editing = false, check = true;
    private static Utilities utilities;
    private ArrayList<TextInputLayout> layouts;
    private ArrayList<EditText> texts;
    private boolean noPassword;

    public UserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserFragment newInstance(String param1, String param2) {
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();
        utilities = Utilities.getInstance();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        //progressDialog.setMessage(Utilities.loading);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        user = utilities.getCurrentUser();
        progressDialog.dismiss();
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProgressDialog progressDialog = new ProgressDialog(view.getContext());
        progressDialog.setMessage(Utilities.verifying);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        layouts = new ArrayList<>();
        texts = new ArrayList<>();
        username = view.findViewById(R.id.username_label);
        username.setText("Hi, " + user.getUsername() + "!");
        name = view.findViewById(R.id.name);
        nameLayout = view.findViewById(R.id.name_layout);
        surname = view.findViewById(R.id.surname);
        surnameLayout = view.findViewById(R.id.surname_layout);
        email = view.findViewById(R.id.email);
        emailLayout = view.findViewById(R.id.email_layout);
        password = view.findViewById(R.id.password);
        passwordLayout = view.findViewById(R.id.password_layout);
        confirmPassword = view.findViewById(R.id.confirm_password);
        confirmPasswordLayout = view.findViewById(R.id.confirm_password_layout);
        layouts.addAll(Arrays.asList(nameLayout, surnameLayout, emailLayout, passwordLayout, confirmPasswordLayout));
        texts.addAll(Arrays.asList(name, surname, email, password, confirmPassword));
        disableAll();
        fillField();
        Button edit = view.findViewById(R.id.edit);
        Button cancel = view.findViewById(R.id.cancel);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editing) {
                    enableAll();
                    editing = true;
                    edit.setText("Confirm");
                    cancel.setBackgroundColor(Color.RED);
                } else {
                    checkName();
                    checkSurname();
                    checkEmail();
                    checkPassword();
                    if (check) {
                        user = utilities.getCurrentUser();
                        Toast t = new Toast(v.getContext());
                        System.out.println(user.getUsername());
                        t.setDuration(Toast.LENGTH_LONG);
                        UserHelperClass userUpdate = new UserHelperClass(name.getText().toString(), surname.getText().toString(), email.getText().toString(), null, user.getAdvanced(), user.getUsername());
                        if (noPassword) {
                            userUpdate.setPassword(user.getPassword());
                        } else userUpdate.setPassword(password.getText().toString());
                        System.out.println(userUpdate.getEmail() + " -- " + utilities.getCurrentUID());
                        Utilities.getInstance().updateUser(userUpdate, utilities.getCurrentUID());
                        t.setText("Done");
                        t.show();
                        edit.setText("Edit");
                        disableAll();
                        editing = false;
                        clearPasswordField();
                        clearError(layouts.size());
                        cancel.setBackgroundColor(Color.BLUE);
                    }
                    check = true;
                }
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
                    clearPasswordField();
                    clearError(layouts.size());
                    fillField();
                }
            }
        });
        progressDialog.dismiss();
    }

    private void fillField() {
        name.setText(user.getName());
        surname.setText(user.getSurname());
        email.setText(user.getEmail());

    }

    private void clearPasswordField() {
        password.setText(null);
        confirmPassword.setText(null);
    }


    private void checkName() {
        if (check) {
            if (name.getText().toString().equals("") || name.getText().toString().isEmpty()) {
                check = false;
                nameLayout.setError(Utilities.signUpNameError);
            }
        }
    }

    private void checkPassword() {

        if (check) {
            noPassword = false;
            clearError(3);
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
}