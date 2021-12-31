package com.domslab.makeit.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

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
/*import com.ortiz.touchview.TouchImageView;*/
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.zolad.zoominimageview.ZoomInImageView;

import java.util.ArrayList;
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
    private ZoomInImageView img;
    private CustomTextView pageText;
    private YoutubePlayer player;
    private float time_elapsed = 0;
    private boolean fullScreen = false;

    private ArrayList<View> views;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("page");
            time_elapsed = savedInstanceState.getFloat("timing");
            fullScreen = savedInstanceState.getBoolean("full_screen");
        }
        setContentView(R.layout.activity_manual_page);
        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);
        pageNum = findViewById(R.id.current_page);
        manualName = findViewById(R.id.manual_name_label);
        body = findViewById(R.id.body);
        exit = findViewById(R.id.exit);
        readManual();
        views = new ArrayList<>();
        views.add(exit);
        views.add(manualName);
        views.add(findViewById(R.id.bottom));


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
                pageText = new CustomTextView(ManualActivity.this);
                pageText.setText(currentManualPage.getItem("text"));

                /*pageText.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                pageText.setGravity(Gravity.CENTER_HORIZONTAL);*/
                views.add(pageText);
                body.addView(pageText);

            }
            if (currentManualPage.hasItem("image")) {
                //pageImage.setVisibility(View.VISIBLE);
                System.out.println(currentManualPage.getItem("image"));
                byte[] decodedString = Base64.getDecoder().decode(currentManualPage.getItem("image"));
                img = new ZoomInImageView(ManualActivity.this);
                img.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                /*img.setMinimumWidth((int) (400 * density));
                img.setMinimumHeight((int) (900 * density));
*/
                ViewGroup.LayoutParams layoutParams = body.getLayoutParams();
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.height = 800;
                img.setLayoutParams(layoutParams);
                views.add(img);
                body.addView(img);
            }
            if (currentManualPage.hasItem("timer")) {
                timer = new Timer(ManualActivity.this, Integer.parseInt(currentManualPage.getItem("timer")));
                body.addView(timer);
                views.add(timer);
                if (time_elapsed == 0)
                    player = new YoutubePlayer(ManualActivity.this, "aE4LELSbqKo", getLifecycle(), views, 0, false, fullScreen);
                else
                    player = new YoutubePlayer(ManualActivity.this, "aE4LELSbqKo", getLifecycle(), views, time_elapsed, true, fullScreen);
                body.addView(player);
            }
        }
    }

    private void reset() {
        previous.setVisibility(View.VISIBLE);
        next.setVisibility(View.VISIBLE);
        if (timer != null) {
            timer.stop();
            views.remove(timer);
            body.removeView(timer);
            timer = null;
        }
        views.remove(img);
        views.remove(pageText);
        body.removeView(img);
        body.removeView(pageText);
        body.removeView(player);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("page", currentPage);
        if (player != null) {
            outState.putFloat("timing", player.getTime());
            outState.putBoolean("full_screen", player.isFullScreen());
        }
    }
}