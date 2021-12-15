package com.domslab.makeit.model;

import android.app.ProgressDialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.domslab.makeit.FirebaseCallBack;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utilities {
    public static final long MAX_FILE_SIZE = 1024 * 1024;
    public static final long MAX_IMAGE_SIZE = 1024 * 20;
    public static final String COVER_SIZE_EXCEEDED = "The size of the cover is too large, it must be 20KB or less";
    public static final String IMAGE_SIZE_EXCEEDED = "The size of the image is too large, it must be 20KB or less";
    public static final String NO_COVER = "Cover doesn't exists, check your file";
    public static final String NO_DESCRIPTION = "Description not found check your file";
    public static final String PAGE_EMPTY = "Page content not found, check page: ";
    public static final String RESTART_NEEDED = "To see your manual on your device you must restart the application";
    public static CharSequence noOldEmail = "You have to enter the old email if you want to change it";
    public static String locationLabel = "Location: ";
    public static String checkUpload = "Are you sure to upload this file?";
    private static String currentUID;
    public static UserHelperClass currentUser;
    public static final String sharedPreferencesName = "logv3";
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
    public static String emailNoMatch = "This email does not match the current one";
    public static String currentPage = "Page: ";
    private static FirebaseDatabase rootNode;
    private static DatabaseReference reference;
    private static FirebaseAuth auth = null;
    private static ProgressDialog progressDialog;

    private Utilities() {
    }

    public static void showProgressDialog(Context context, boolean loading) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            if (loading)
                progressDialog.setMessage(Utilities.loading);
            else
                progressDialog.setMessage(Utilities.verifying);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    public static void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }


    public static FirebaseAuth getAuthorisation() {
        if (auth == null)
            auth = FirebaseAuth.getInstance();
        return auth;
    }

    public static void setCurrentUsername(String user) {
        currentUID = user;
        //setUser();
    }

    private static void setUser(UserHelperClass user) {
        rootNode = FirebaseDatabase.getInstance(Utilities.path);
        reference = rootNode.getReference("users");
        readData(new FirebaseCallBack() {
            @Override
            public void onCallBack(List<String> list, boolean business, boolean wait) {
                currentUser = new UserHelperClass(list.get(0), list.get(1), list.get(2), business, list.get(3), wait);
            }
        });
    }


    private static void readData(FirebaseCallBack callBack) {
        Query checkUser = reference.orderByChild(currentUID);
        ArrayList<String> userData = new ArrayList<>();
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = "", surname = "", email = "", username = "";
                boolean business = false, waiting = false;
                UserHelperClass userHelperClass = null;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot o : dataSnapshot.getChildren())
                        if (o.getKey().equals(currentUID)) {
                            name = o.child("name").getValue().toString();
                            surname = o.child("surname").getValue().toString();
                            email = o.child("email").getValue().toString();
                            business = (boolean) o.child("advanced").getValue();
                            username = o.child("username").getValue().toString();
                            waiting = (boolean) o.child("waiting").getValue();
                            userData.addAll(Arrays.asList(name, surname, email, username));
                            callBack.onCallBack(userData, business, waiting);
                            userHelperClass = new UserHelperClass(name, surname, email, business, username, waiting);
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


    public static String getCurrentUID() {
        return currentUID;
    }
}
