package com.domslab.makeit.view;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.domslab.makeit.model.Utilities;
import com.domslab.makeit.model.Manual;
import com.domslab.makeit.model.ManualPage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ManualActivity extends AppCompatActivity {
    //manuali = hashmap <String,Arraylist>
    private Manual manual;
    private SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utilities.showProgressDialog(ManualActivity.this, true);
        preferences = getSharedPreferences(Utilities.sharedPreferencesName,MODE_PRIVATE);
        readManual();
    }

    private void readManual() {
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference("manual");
        Query checkUser = reference.orderByChild("provaC");
        manual = new Manual();
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int counter = 1;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot o : dataSnapshot.getChildren()) {
                        if (o.hasChild("name"))
                            manual.setName(o.child("name").getValue().toString());
                        if (o.hasChild("date"))
                            manual.setDate(o.child("date").getValue().toString());
                        if (o.hasChild("description"))
                            manual.setDescription(o.child("description").getValue().toString());
                        if (o.hasChild("cover"))
                            manual.setCover(o.child("cover").getValue().toString());
                        if (o.hasChild("content")) {
                            DataSnapshot content = o.child("content");

                            while (true) {
                                ManualPage manualPage = new ManualPage();
                                if (content.hasChild(Integer.toString(counter))) {
                                    DataSnapshot snapshot = content.child(Integer.toString(counter)).child("pageContent");
                                    if (snapshot.hasChild("image"))
                                        manualPage.add("image", snapshot.child("image").getValue().toString());
                                    if (snapshot.hasChild("text"))
                                        manualPage.add("text", snapshot.child("text").getValue().toString());
                                    System.out.println("Pagina " + Integer.toString(counter) + " \n" + manualPage.toString());
                                    manual.addPage(Integer.toString(counter), manualPage);
                                    counter++;
                                } else {
                                    System.out.println("else");
                                    break;
                                }
                            }
                        }
                    }
                }
                Utilities.closeProgressDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}