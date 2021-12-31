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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;

public class YoutubePlayer extends ConstraintLayout {
    private YouTubePlayerView player;
    private Activity activity;
    private float seconds;

    public YoutubePlayer(@NonNull Context context, String id, Lifecycle lifecycle, ArrayList<View> views, float time, boolean autostart, boolean fullscreen) {
        super(context);
        inflate(getContext(), R.layout.yt_player, this);
        this.activity = (Activity) context;
        player = findViewById(R.id.yt_player);
        lifecycle.addObserver(player);

        player.setEnableAutomaticInitialization(false);
        player.initialize(new YouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                if (!autostart)
                    youTubePlayer.cueVideo(id, time);
                else youTubePlayer.loadVideo(id, time);
            }

            @Override
            public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState playerState) {

            }

            @Override
            public void onPlaybackQualityChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlaybackQuality playbackQuality) {

            }

            @Override
            public void onPlaybackRateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlaybackRate playbackRate) {

            }

            @Override
            public void onError(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerError playerError) {

            }

            @Override
            public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float v) {
                seconds = v;
            }

            @Override
            public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float v) {

            }

            @Override
            public void onVideoLoadedFraction(@NonNull YouTubePlayer youTubePlayer, float v) {

            }

            @Override
            public void onVideoId(@NonNull YouTubePlayer youTubePlayer, @NonNull String s) {

            }

            @Override
            public void onApiChange(@NonNull YouTubePlayer youTubePlayer) {

            }
        });
        player.addFullScreenListener(new YouTubePlayerFullScreenListener() {
            @Override
            public void onYouTubePlayerEnterFullScreen() {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                hideSystemUi();
                System.out.println(player.isFullScreen());
                for (View v : views) {
                    v.setVisibility(GONE);
                    v.invalidate();
                }
                //player.enterFullScreen();

            }

            @Override
            public void onYouTubePlayerExitFullScreen() {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                showSystemUi();
                System.out.println(player.isFullScreen());
                for (View v : views) {
                    v.setVisibility(VISIBLE);
                    v.invalidate();
                }

                //player.exitFullScreen();

            }
        });
        if (fullscreen)
            player.enterFullScreen();


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

    public float getTime() {
        return seconds;
    }

    public boolean isFullScreen() {
        return player.isFullScreen();
    }
}
