package com.domslab.makeit.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.domslab.makeit.R;
import com.domslab.makeit.adapters.ManualAdapter;
import com.domslab.makeit.model.ManualCard;
import com.domslab.makeit.model.Utilities;
import com.domslab.makeit.view.menu.HomeContainer;
import com.domslab.makeit.view.pagerFragment.FavouritesFragment;
import com.domslab.makeit.view.pagerFragment.MyManualFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class HomeManual extends AppCompatActivity {
    private TextView name;
    private TextView descriptionContent;
    private ImageView cover;
    private ImageButton exit;
    private TextView date;
    private Button start;
    private String id;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_manual);
        name = findViewById(R.id.home_manual_name);
        descriptionContent = findViewById(R.id.description);
        cover = findViewById(R.id.cover);
        start = findViewById(R.id.start);
        date = findViewById(R.id.date);
        exit = findViewById(R.id.home_exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeManual.this, HomeContainer.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();

            }
        });
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id = extras.getString("manualId");
            loadInfo(id);
        }
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeManual.this, ManualActivity.class);
                intent.putExtra("manualId", id);
                startActivity(intent);
            }
        });

    }


    private void loadInfo(String manualId) {
        id = manualId;
        Utilities.showProgressDialog(HomeManual.this, true);
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference("manual");
        Query checkUser = reference;
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    for (DataSnapshot o : dataSnapshot.getChildren()) {
                        if (o.getKey().equals(id)) {
                            if (o.hasChild("name"))
                                name.setText(o.child("name").getValue().toString());
                            if (o.hasChild("description"))
                                descriptionContent.setText(o.child("description").getValue().toString());
                            if (o.hasChild("date"))
                                date.setText(o.child("date").getValue().toString());
                        }
                    }
                    Utilities.closeProgressDialog();
                    loadCover(id);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadCover(String id) {
        Utilities.showProgressDialog(HomeManual.this, true);
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://makeit-27047.appspot.com/");
        gsReference.child(id + "/cover").getBytes(Utilities.MAX_FILE_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                String decodedString = new String(bytes);
                System.out.println(decodedString);
                byte[] coded = android.util.Base64.decode(decodedString, Base64.DEFAULT);
                cover.setImageBitmap(BitmapFactory.decodeByteArray(coded, 0, coded.length));
                Utilities.closeProgressDialog();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utilities.closeProgressDialog();
            }
        });
    }

}