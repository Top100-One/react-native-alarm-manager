package com.baekgol.reactnativealarmmanager.util;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.Objects;

public class AlarmService extends Service {
    private String packageName;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private final String channelId = "alarm";

    @Override
    public void onCreate() {
        super.onCreate();
        packageName = getPackageName();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mediaPlayer!=null) {
            vibrator.cancel();
            mediaPlayer.stop();
        }

        Intent notiIntent = new Intent(this, getMainActivity());
        notiIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        notiIntent.putExtra("id", intent.getIntExtra("id", 0));
        notiIntent.putExtra("hour", intent.getIntExtra("hour", 0));
        notiIntent.putExtra("minute", intent.getIntExtra("minute", 0));
        notiIntent.putExtra("notiRemovable", intent.getBooleanExtra("notiRemovable", true));

        PendingIntent notiPendingIntent = PendingIntent.getActivity(this, 0, notiIntent, PendingIntent.FLAG_IMMUTABLE);

        @SuppressLint("DiscouragedApi") NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(intent.getStringExtra("title"))
                .setContentText(intent.getStringExtra("text"))
                .setSmallIcon(getResources().getIdentifier(intent.getStringExtra("icon"), "drawable", packageName))
                .setContentIntent(notiPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        this.startForeground(1, builder.build());

        if(intent.getBooleanExtra("vibration", true)){
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createWaveform(new long[]{1000, 500}, new int[]{0, 50}, 0));
        }

        @SuppressLint("DiscouragedApi") int resId = this.getResources().getIdentifier(intent.getStringExtra("sound"), "raw", packageName);

        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .setLegacyStreamType(AudioManager.STREAM_ALARM)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());

            // Set the data source for the MediaPlayer
            mediaPlayer.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));

            // Prepare the MediaPlayer asynchronously
            mediaPlayer.prepareAsync();

            // Set a listener to start playing when prepared
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // Start playing the alarm sound
                    mediaPlayer.start();
                }
            });

            // Set looping behavior
            mediaPlayer.setLooping(true);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mediaPlayer!=null) {
            if(vibrator!=null) vibrator.cancel();
            mediaPlayer.release();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Class getMainActivity(){
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        String className = Objects.requireNonNull(intent.getComponent()).getClassName();

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
