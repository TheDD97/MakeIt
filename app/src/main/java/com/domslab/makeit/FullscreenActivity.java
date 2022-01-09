package com.domslab.makeit;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.domslab.makeit.view.YoutubePlayer;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_fullscreen);
        container = findViewById(R.id.container);
        //mVisible = true;
        //mControlsView = binding.fullscreenContentControls;
        //mContentView = binding.fullscreenContent;


        //mContentView =binding.fullscreenContent;
        // Set up the user interaction to manually show or hide the system UI.
        /*mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });*/

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            YoutubePlayer player = new YoutubePlayer(FullscreenActivity.this, extras.getString("id"), getLifecycle(), extras.getFloat("time"));
            player.setFullScreen();
            container.addView(player);
        }
        //player.start();
        //YoutubePlayer player = new YoutubePlayer(FullscreenActivity.this, savedInstanceState.getString("id"), getLifecycle(), savedInstanceState.getFloat("time"));
        //
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            // Set the content to appear under the system bars so that the
                            // content doesn't resize when the system bars hide and show.
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }
}