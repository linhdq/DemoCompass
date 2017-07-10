package com.ldz.fpt.democompass;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.VideoView;

public class LauncherActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        init();
    }

    private void init() {
        videoView = (VideoView) findViewById(R.id.video_view);
        videoView.setMediaController(null);
        videoView.setOnCompletionListener(this);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.clip_start;
        videoView.setVideoURI(Uri.parse(path));
        videoView.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
