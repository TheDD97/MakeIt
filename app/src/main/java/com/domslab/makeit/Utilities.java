package com.domslab.makeit;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Utilities {
    private static String currentUID;
    private static UserHelperClass currentUser;
    public static final String sharedPreferencesName = "logv2";
    public static final String verifying = "Verifying...";
    public static final String loading = "Loading...";
    public static final String path = "https://makeit-27047-default-rtdb.europe-west1.firebasedatabase.app/";
    public static final String noUsername = "Empty Username field";
    public static final String noPassword = "Empty Password field";
    public static final String signUpNameError = "Check name";
    public static final String signUpEmptyEmail = "Empty Email";
    public static final String signUpWrongEmailFormat = "Check Email";
    public static final String signUpSurnameError = "Check Surname";
    public static final String signUpUsernameAlreadyExists = "Username already exists";
    public static final String signUpEmptyUsername = "Check Username";
    public static final String noConfirmPassword = "Empty Confirm Password field";
    public static final String noBothPsw = "The passwords entered are different";
    public static final String passwordFormat = "The password must contain at least 8 characters, an uppercase character, a special character and a number";
    private static FirebaseDatabase rootNode;
    private static DatabaseReference reference;
    private static Utilities instance = null;
    private static FirebaseAuth auth = null;

    private Utilities() {
    }

    public static Utilities getInstance() {
        if (instance == null)
            instance = new Utilities();
        return instance;
    }


    public static FirebaseAuth getAuthorisation() {
        if (auth == null)
            auth = FirebaseAuth.getInstance();
        return auth;
    }

    public void setCurrentUsername(String user, SharedPreferences.Editor editor) {
        currentUID = user;
        setUser(editor);
    }

    private void setUser(SharedPreferences.Editor editor) {
        rootNode = FirebaseDatabase.getInstance(Utilities.path);
        reference = rootNode.getReference("users");
        Query checkUser = reference.orderByChild(currentUID);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = "", surname = "", email = "", password = "", username = "";
                boolean business = false;
                UserHelperClass userHelperClass = null;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot o : dataSnapshot.getChildren())
                        if (o.getKey().equals(currentUID)) {
                            name = o.child("name").getValue().toString();
                            surname = o.child("surname").getValue().toString();
                            email = o.child("email").getValue().toString();
                            password = o.child("password").getValue().toString();
                            business = (boolean) o.child("advanced").getValue();
                            username = o.child("username").getValue().toString();
                            editor.putBoolean("advanced", business);
                            editor.apply();
                            userHelperClass = new UserHelperClass(name, surname, email, password, business, username);
                            currentUser = userHelperClass;
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void updateUser(UserHelperClass userHelperClass, String id) {
        rootNode = FirebaseDatabase.getInstance(Utilities.path);
        reference = rootNode.getReference("users");
        reference.child(id).setValue(userHelperClass);
        currentUser = userHelperClass;
    }

    public static void clear() {
        currentUser = null;
        currentUID = null;
        auth = null;
    }

    public static UserHelperClass getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentUID() {
        return currentUID;
    }
}
