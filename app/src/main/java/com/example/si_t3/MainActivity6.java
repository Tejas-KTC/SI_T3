package com.example.si_t3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

public class MainActivity6 extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    private ImageView imgPlayPause;
    private TextView txtFileName;
    private boolean isPlaying = true; // Start playing immediately

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);

        // Initialize UI components
        playerView = findViewById(R.id.playerView);
        imgPlayPause = findViewById(R.id.imgPlayPause);
        txtFileName = findViewById(R.id.txtFileName);

        // Get file path from Intent
        Intent intent = getIntent();
        String filePath = intent.getStringExtra("filePath");
        String fileName = intent.getStringExtra("fileName");

        // Set file name to TextView
        txtFileName.setText(fileName);

        // Initialize ExoPlayer
        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);

        // Set the media item
        Uri videoUri = Uri.parse(filePath);
        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();

        // Play/Pause button functionality
        imgPlayPause.setOnClickListener(v -> {
            if (exoPlayer.isPlaying()) {
                exoPlayer.pause();
                imgPlayPause.setImageResource(R.drawable.play); // Set Play Icon
            } else {
                exoPlayer.play();
                imgPlayPause.setImageResource(R.drawable.pause); // Set Pause Icon
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}
