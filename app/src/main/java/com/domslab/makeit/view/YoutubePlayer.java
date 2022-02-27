package com.domslab.makeit.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Lifecycle;

import com.domslab.makeit.R;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;


public class YoutubePlayer extends ConstraintLayout {
    private YouTubePlayerView player;
    private Activity activity;
    private float seconds;
    private String id;
    private YouTubePlayerFullScreenListener zoomIn;
    private YouTubePlayerFullScreenListener zoomOut;

    public YoutubePlayer(@NonNull Context context, String id, Lifecycle lifecycle, float time) {
        super(context);
        inflate(getContext(), R.layout.yt_player, this);
        this.activity = (Activity) context;
        player = findViewById(R.id.yt_player);
        lifecycle.addObserver(player);
        this.id = id;
        player.setEnableAutomaticInitialization(false);
        zoomIn = new YouTubePlayerFullScreenListener() {
            @Override
            public void onYouTubePlayerEnterFullScreen() {
                Intent intent = new Intent(activity, FullScreenActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("time", getTime());
                activity.startActivity(intent);
                player.exitFullScreen();
            }

            @Override
            public void onYouTubePlayerExitFullScreen() {
            }
        };
        zoomOut = new YouTubePlayerFullScreenListener() {
            @Override
            public void onYouTubePlayerEnterFullScreen() {
                SharedPreferences.Editor editor = activity.getSharedPreferences("video", Context.MODE_PRIVATE).edit();
                editor.putFloat("time", getTime());
                editor.apply();
                activity.onBackPressed();
            }

            @Override
            public void onYouTubePlayerExitFullScreen() {

            }
        };
        player.addFullScreenListener(zoomIn);
        player.initialize(new YouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.cueVideo(id, time);
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
    }

    public float getTime() {
        return seconds;
    }

    public boolean isFullScreen() {
        return player.isFullScreen();
    }

    public void setFullScreen() {
        player.removeFullScreenListener(zoomIn);
        player.addFullScreenListener(zoomOut);
    }
}
