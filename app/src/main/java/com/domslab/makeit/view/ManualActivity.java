package com.domslab.makeit.view;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.domslab.makeit.R;
import com.domslab.makeit.model.Utilities;
import com.domslab.makeit.model.Manual;
import com.domslab.makeit.model.ManualPage;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
    private SwipeListener swipeListener;
    private ScrollView content;
    private ArrayList<View> views;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("page");
            time_elapsed = savedInstanceState.getFloat("time");
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
        SharedPreferences preferences = getSharedPreferences("video", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        for (int i = 0; i < body.getChildCount(); ++i)
            if (body.getChildAt(i).getClass().equals(YoutubePlayer.class)) {
                body.removeView(body.getChildAt(i));
                player = new YoutubePlayer(ManualActivity.this, currentManualPage.getItem("yt_video"), getLifecycle(), preferences.getFloat("time", 0));
                body.addView(player);
                break;
            }
        editor.remove("time").apply();
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
        content = findViewById(R.id.content);
        swipeListener = new SwipeListener(content);
    }

    private void readManual() {
        Utilities.showProgressDialog(ManualActivity.this);
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
                                            if (snapshot.hasChild("image"))
                                                loadImage(currentManual, Integer.toString(counter), manualPage);
                                            if (snapshot.hasChild("text"))
                                                manualPage.add("text", snapshot.child("text").getValue().toString());
                                            if (snapshot.hasChild("timer")) {
                                                manualPage.add("timer", snapshot.child("timer").getValue().toString());
                                            }
                                            if (snapshot.hasChild("yt_video"))
                                                manualPage.add("yt_video", snapshot.child("yt_video").getValue().toString());
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
        Utilities.showProgressDialog(ManualActivity.this);
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
                body.addView(pageText);
            }
            if (currentManualPage.hasItem("image")) {
                byte[] decodedString = Base64.getDecoder().decode(currentManualPage.getItem("image"));
                img = new ZoomInImageView(ManualActivity.this);
                img.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                ViewGroup.LayoutParams layoutParams = body.getLayoutParams();
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.height = 800;
                img.setLayoutParams(layoutParams);
                body.addView(img);
            }
            if (currentManualPage.hasItem("timer")) {
                timer = new Timer(ManualActivity.this, Integer.parseInt(currentManualPage.getItem("timer")));
                body.addView(timer);
            }
            if (currentManualPage.hasItem("yt_video")) {
                if (time_elapsed == 0)
                    player = new YoutubePlayer(ManualActivity.this, currentManualPage.getItem("yt_video"), getLifecycle(), 0);
                else
                    player = new YoutubePlayer(ManualActivity.this, currentManualPage.getItem("yt_video"), getLifecycle(), time_elapsed);
                body.addView(player);
            }
        }
    }

    private void reset() {
        previous.setVisibility(View.VISIBLE);
        next.setVisibility(View.VISIBLE);
        if (timer != null) {
            timer.stop();
            body.removeView(timer);
            timer = null;
        }
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
            outState.putFloat("time", player.getTime());
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        SharedPreferences preferences = getSharedPreferences("video", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        for (int i = 0; i < body.getChildCount(); ++i)
            if (body.getChildAt(i).getClass().equals(YoutubePlayer.class)) {
                body.removeView(body.getChildAt(i));
                player = new YoutubePlayer(ManualActivity.this, currentManualPage.getItem("yt_video"), getLifecycle(), preferences.getFloat("time", 0));
                body.addView(player);
                break;
            }
        editor.remove("time").apply();
    }

    private class SwipeListener implements View.OnTouchListener {
        GestureDetector gestureDetector;

        public SwipeListener(View view) {
            int threshold = 100;
            int velocity_threshold = 100;
            GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    float xDiff = e2.getX() - e1.getX();
                    float yDiff = e2.getY() - e2.getY();
                    try {
                        if (Math.abs(xDiff) > Math.abs(yDiff)) {
                            if (Math.abs(xDiff) > threshold && Math.abs(velocityX) > velocity_threshold) {
                                if (xDiff > 0) {
                                    //swipe right
                                    if (currentPage > 1) {
                                        setCurrentPage(--currentPage);
                                    }
                                } else {
                                    //swipe left
                                    if (currentPage < manual.size()) {
                                        setCurrentPage(++currentPage);
                                    }
                                }
                                return true;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            };
            gestureDetector = new GestureDetector(listener);
            view.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return gestureDetector.onTouchEvent(motionEvent);
        }
    }
}