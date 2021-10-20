package com.domslab.makeit;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Utilities {
    public static String currentUsername;
    public static UserHelperClass currentUser;
    public static String path = "https://makeit-27047-default-rtdb.europe-west1.firebasedatabase.app/";
    public static String noUsername = "Empty Username field";
    public static String noPassword = "Empty Password field";
    public static String loginError = "Wrong username or password";
    public static String noUser = "No such user";
    public static String noConfirmPassword = "Empty Confirm Password field";
    public static String noBothPsw = "The passwords entered are different";
    public static String passwordFormat = "The password must contain at least 8 characters, an uppercase character, a special character and a number";

    public static void setCurrentUsername(String username) {
        currentUsername = username;
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference("user");
        Query checkUser = reference.orderByChild(currentUsername);
        System.out.println("IM HEREEE ---- " + currentUsername);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = "", surname = "", email = "", password = "";
                boolean business = false;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot o : dataSnapshot.getChildren())
                        if (o.getKey().equals(currentUsername)) {
                            name = o.child("name").getValue().toString();
                            surname = o.child("surname").getValue().toString();
                            email = o.child("email").getValue().toString();
                            password = o.child("password").getValue().toString();
                            business = (boolean) o.child("business").getValue();
                            currentUser = new UserHelperClass(name, surname, email, password, business);
                            System.out.println(currentUser.getName() + " -- " + currentUser.getEmail() + " -- " + currentUser.isBusiness());
                            return;
                        }


                }

                System.out.println("finish");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static void clear() {
        currentUser = null;
        currentUsername = null;
    }
}
