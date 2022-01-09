package com.domslab.makeit.view;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.domslab.makeit.R;

public class Timer extends ConstraintLayout {
    private ProgressBar progressBar;
    private TextView currentTime;
    private Button start;
    private int currentTimeVal;
    private int time;
    private Button reset;
    private String minute, second;
    private android.os.Handler handler;
    private Runnable r;
    boolean countdownStarted = false;
    boolean stop = false;

    public Timer(Context context, int time) {
        super(context);

        inflate(getContext(), R.layout.timer, this);

        progressBar = findViewById(R.id.progress_bar);
        this.time = time;
        progressBar.setMax(time);
        currentTime = findViewById(R.id.timer);
        start = findViewById(R.id.start);
        reset = findViewById(R.id.reset);
        currentTimeVal = time;
        start.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!countdownStarted) {
                    countdownStarted = true;
                    start.setText("Stop");
                    handler = new Handler();
                    stop = false;
                    r = new Runnable() {
                        @Override
                        public void run() {
                            if (!stop) {
                                if (currentTimeVal > 0) {
                                    currentTimeVal--;
                                    update();
                                    handler.postDelayed(this, 1000);
                                } else {
                                    stop = true;
                                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                    NotificationChannel channel = new NotificationChannel("makeIt", "Tempo", NotificationManager.IMPORTANCE_DEFAULT);
                                    channel.setDescription("Il tempo è scaduto");
                                    NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
                                    notificationManager.createNotificationChannel(channel);
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), channel.getId())
                                            .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                                            .setContentTitle("Affrettati!")
                                            .setContentText("Il tempo è scaduto!!")
                                            .setSound(alarmSound)
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getContext());
                                    notificationManagerCompat.notify(100, builder.build());
                                }
                            }
                        }
                    };
                    handler.post(r);
                }else{
                    countdownStarted = false;
                    start.setText("start");
                    stop = true;
                }
            }
        });
        reset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTimeVal = time;
                update();
                stop = true;
                countdownStarted = false;
            }
        });
        update();
    }

    private void update() {
        minute = Integer.toString(currentTimeVal / 60);
        second = Integer.toString(currentTimeVal - (currentTimeVal / 60) * 60);
        currentTime.setText(minute + ":" + second);
        progressBar.setProgress(time - currentTimeVal);
    }

    public void stop() {
        currentTimeVal = time;
        update();
        stop = true;
        countdownStarted = false;
    }
}
