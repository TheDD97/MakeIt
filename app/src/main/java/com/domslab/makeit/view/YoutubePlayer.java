package com.domslab.makeit.view;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Lifecycle;

import com.domslab.makeit.R;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;

public class YoutubePlayer extends RelativeLayout {
    private YouTubePlayerView player;
    private Activity activity;

    public YoutubePlayer(@NonNull Context context, String id, Lifecycle lifecycle, ArrayList<View> views) {
        super(context);
        inflate(getContext(), R.layout.yt_player, this);
        this.activity = (Activity) context;
        player = findViewById(R.id.yt_player);
        lifecycle.addObserver(player);
        player.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.cueVideo(id, 0);
            }
        });
        player.addFullScreenListener(new YouTubePlayerFullScreenListener() {
            @Override
            public void onYouTubePlayerEnterFullScreen() {
                //activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                hideSystemUi();

                System.out.println(player.isFullScreen());
                for (View v : views) {
                    v.setVisibility(GONE);
                    v.invalidate();
                }
                player.enterFullScreen();

            }

            @Override
            public void onYouTubePlayerExitFullScreen() {
                //activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                showSystemUi();
                System.out.println(player.isFullScreen());
                for (View v : views) {
                    v.setVisibility(VISIBLE);
                    v.invalidate();
                }

                player.exitFullScreen();

            }
        });
    }

    private void showSystemUi() {
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private void hideSystemUi() {
        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

    }
}
