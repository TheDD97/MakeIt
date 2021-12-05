package com.domslab.makeit.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.domslab.makeit.R;
import com.domslab.makeit.adapters.ManualAdapter;
import com.domslab.makeit.model.ManualCard;
import com.domslab.makeit.model.Utilities;
import com.domslab.makeit.view.pagerFragment.FavouritesFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Base64;

public class HomeManual extends AppCompatActivity {
    private TextView name;
    private TextView descriptionContent;
    private ImageView cover;
    private Button start;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_manual);
        name = findViewById(R.id.home_manual_name);
        descriptionContent = findViewById(R.id.description);
        cover = findViewById(R.id.cover);
        start = findViewById(R.id.start);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id = extras.getString("manualId");
            loadInfo(id);
        }
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeManual.this,ManualActivity.class);
                intent.putExtra("manualId",id);
                startActivity(intent);
            }
        });
    }


    private void loadInfo(String manualId) {
        id = manualId;
        Utilities.showProgressDialog(HomeManual.this, true);

        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference();
        Query checkUser = reference.child("manual");
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    for (DataSnapshot o : dataSnapshot.getChildren()) {

                        if (o.hasChild("name"))
                            name.setText(o.child("name").getValue().toString());
                        if (o.hasChild("description"))
                            descriptionContent.setText(o.child("description").getValue().toString());
                        /*if (o.hasChild("date"))
                            date = o.child("date").getValue().toString();*/
                        if (o.hasChild("cover")) {
                            byte[] decodedString = Base64.getDecoder().decode(o.child("cover").getValue().toString());
                            cover.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                        }
                    }
                    Utilities.closeProgressDialog();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}