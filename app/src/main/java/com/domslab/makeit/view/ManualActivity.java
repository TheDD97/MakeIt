package com.domslab.makeit.view;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.domslab.makeit.R;
import com.domslab.makeit.model.Utilities;
import com.domslab.makeit.model.Manual;
import com.domslab.makeit.model.ManualPage;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Base64;

public class ManualActivity extends AppCompatActivity {
    //manuali = hashmap <String,Arraylist>
    private Manual manual;
    private SharedPreferences preferences;
    private int currentPage = 1;
    private ImageButton exit;
    private ImageButton next;
    private ImageButton previous;
    private ImageView pageImage;
    private TextView pageText;
    private TextView pageNum;
    private TextView manualName;
    private ManualPage currentManualPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
        Utilities.showProgressDialog(ManualActivity.this, true);
        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);
        pageImage = findViewById(R.id.page_image);
        pageText = findViewById(R.id.page_text);
        pageNum = findViewById(R.id.current_page);
        manualName = findViewById(R.id.manual_name_label);
        readManual();
        next.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                setCurrentPage(++currentPage);
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                setCurrentPage(--currentPage);
            }
        });
        exit = findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void readManual() {
        String currentManual;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentManual = extras.getString("manualId");
            System.out.println(currentManual);
            FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
            DatabaseReference reference = rootNode.getReference("manual");
            Query checkUser = reference.orderByChild(currentManual);
            manual = new Manual();
            checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
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
                                            manualPage.add("image", snapshot.child("image").getValue().toString().trim());
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
                                manualName.setText(manual.getName());
                                setCurrentPage(currentPage);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setCurrentPage(int currentPage) {
        pageNum.setText(Utilities.currentPage + Integer.toString(currentPage));
        reset();
        currentManualPage = manual.getPage(Integer.toString(currentPage));
        if (currentManualPage != null) {
            if (currentPage == 1)
                previous.setVisibility(View.INVISIBLE);
            if (currentPage == manual.size())
                next.setVisibility(View.INVISIBLE);
            if (currentManualPage.hasItem("text")) {
                pageText.setVisibility(View.VISIBLE);
                pageText.setText((String) currentManualPage.getItem("text"));
            }
            if (currentManualPage.hasItem("image")) {
                pageImage.setVisibility(View.VISIBLE);
                System.out.println(currentManualPage.getItem("image"));
                byte[] decodedString = Base64.getDecoder().decode(currentManualPage.getItem("image"));
                pageImage.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
            }
        }


    }

    private void reset() {
        previous.setVisibility(View.VISIBLE);
        next.setVisibility(View.VISIBLE);
        pageImage.setVisibility(View.INVISIBLE);
        pageText.setVisibility(View.INVISIBLE);
    }
}