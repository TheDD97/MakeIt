package com.domslab.makeit.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.domslab.makeit.R;
import com.domslab.makeit.model.Utilities;
import com.domslab.makeit.model.Manual;
import com.domslab.makeit.model.ManualPage;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.domslab.makeit.view.menu.HomeContainer;
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
import com.ortiz.touchview.TouchImageView;

import java.util.Base64;

public class ManualActivity extends AppCompatActivity {
    private Manual manual;
    private int currentPage = 1;
    private ImageButton exit;
    private ImageButton next;
    private ImageButton previous;
    private TextView pageNum;
    private TextView manualName;
    private ManualPage currentManualPage;
    private LinearLayout body;
    private Timer timer;
    private TouchImageView img;
    private TextView pageText;
    private float density;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_page);
        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);
        //pageImage = findViewById(R.id.page_image);

        //pageText = findViewById(R.id.page_text);
        pageNum = findViewById(R.id.current_page);
        manualName = findViewById(R.id.manual_name_label);
        body = findViewById(R.id.body);
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
        Utilities.showProgressDialog(ManualActivity.this, true);
        String currentManual;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentManual = extras.getString("manualId");

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
                            if (o.getKey().equals(currentManual)) {
                                if (o.hasChild("name"))
                                    manual.setName(o.child("name").getValue().toString());
                                if (o.hasChild("content")) {
                                    DataSnapshot content = o.child("content");
                                    while (true) {
                                        ManualPage manualPage = new ManualPage();
                                        if (content.hasChild(Integer.toString(counter))) {
                                            DataSnapshot snapshot = content.child(Integer.toString(counter)).child("pageContent");
                                            System.out.println(snapshot.getValue());
                                            if (snapshot.hasChild("image"))
                                                loadImage(currentManual, Integer.toString(counter), manualPage);
                                            if (snapshot.hasChild("text"))
                                                manualPage.add("text", snapshot.child("text").getValue().toString());
                                            if (snapshot.hasChild("timer")) {
                                                System.out.println("trovato");
                                                manualPage.add("timer", snapshot.child("timer").getValue().toString());
                                                System.out.println(snapshot.child("timer").getValue().toString());
                                            }
                                            manual.addPage(Integer.toString(counter), manualPage);
                                            counter++;
                                        } else {
                                            break;
                                        }
                                    }
                                }
                                manualName.setText(manual.getName());
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void loadImage(String currentManual, String id, ManualPage manualPage) {
        Utilities.showProgressDialog(ManualActivity.this, true);
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://makeit-27047.appspot.com/");
        gsReference.child(currentManual + "/image" + id).getBytes(Utilities.MAX_FILE_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                String decodedString = new String(bytes);
                manualPage.add("image", decodedString);
                Utilities.closeProgressDialog();
                setCurrentPage(currentPage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utilities.closeProgressDialog();
            }
        });
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
                pageText = new TextView(ManualActivity.this);
                pageText.setText(currentManualPage.getItem("text"));

                /*pageText.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                pageText.setGravity(Gravity.CENTER_HORIZONTAL);*/
                body.addView(pageText);

            }
            if (currentManualPage.hasItem("image")) {
                //pageImage.setVisibility(View.VISIBLE);
                System.out.println(currentManualPage.getItem("image"));
                byte[] decodedString = Base64.getDecoder().decode(currentManualPage.getItem("image"));
                density = getResources().getDisplayMetrics().density;
                System.out.println("DENS" + density);
                img = new TouchImageView(ManualActivity.this);
                img.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                /*img.setMinimumWidth((int) (400 * density));
                img.setMinimumHeight((int) (900 * density));
*/
                ViewGroup.LayoutParams layoutParams = body.getLayoutParams();
                layoutParams.width = 800;
                layoutParams.height = 800;
                img.setLayoutParams(layoutParams);
                body.addView(img);
            }
            if (currentManualPage.hasItem("timer")) {
                System.out.println("si timer");
                timer = new Timer(ManualActivity.this, Integer.parseInt(currentManualPage.getItem("timer")));
                body.addView(timer);
            }
        }
    }

    private void reset() {
        previous.setVisibility(View.VISIBLE);
        next.setVisibility(View.VISIBLE);
        //pageImage.setVisibility(View.INVISIBLE);
        //pageText.setVisibility(View.INVISIBLE);
        if (timer != null) {
            timer.stop();
            body.removeView(timer);
            timer = null;
        }
        body.removeView(img);
        body.removeView(pageText);
    }

    @Override
    public void onBackPressed() {

    }
}