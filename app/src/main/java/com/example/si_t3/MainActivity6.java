package com.example.si_t3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MainActivity6 extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    private ImageView imgPlayPause, imgNext, imgPrev;
    private SeekBar videoSeekbar;
    private TextView tvStartTime, tvEndTime;
    private Handler handler = new Handler();

    private ArrayList<File> videoFiles;
    private int currentVideoIndex = 0;
    private String folderName;
    private String currentFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);

        playerView = findViewById(R.id.playerView);
        imgPlayPause = findViewById(R.id.imgPlayPause);
        imgNext = findViewById(R.id.imgNext);
        imgPrev = findViewById(R.id.imgPrev);
        videoSeekbar = findViewById(R.id.videoSeekbar);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);

        // Get Intent Data
        Intent intent = getIntent();
        if (intent != null) {
            folderName = intent.getStringExtra("folderName");
            currentFileName = intent.getStringExtra("fileName");
        }

        // Load Videos from Folder
        loadVideoFiles();

        // Find the Current File Index
        if (!videoFiles.isEmpty()) {
            findCurrentVideoIndex();
            initializePlayer(currentVideoIndex);
        }

        // Play/Pause Button Click
        imgPlayPause.setOnClickListener(v -> togglePlayPause());

        // Next Video Button Click
        imgNext.setOnClickListener(v -> playNextVideo());

        // Previous Video Button Click
        imgPrev.setOnClickListener(v -> playPreviousVideo());

        // SeekBar Listener
        videoSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && exoPlayer != null) {
                    exoPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Start SeekBar Update Loop
        updateSeekBar();
    }

    private void loadVideoFiles() {
        videoFiles = new ArrayList<>();
        if (folderName != null) {
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), folderName);
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".mp4"));
            if (files != null) {
                videoFiles.addAll(Arrays.asList(files));
                Collections.reverse(videoFiles);
                Log.i("files", String.valueOf(videoFiles));
            }
        }
    }

    private void findCurrentVideoIndex() {
        for (int i = 0; i < videoFiles.size(); i++) {
            if (videoFiles.get(i).getName().equals(currentFileName)) {
                currentVideoIndex = i;
                break;
            }
        }
    }

    private void initializePlayer(int index) {
        if (index >= 0 && index < videoFiles.size()) {
            Uri videoUri = Uri.fromFile(videoFiles.get(index));

            if (exoPlayer != null) {
                exoPlayer.release();
            }
            exoPlayer = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(exoPlayer);
            exoPlayer.setMediaItem(MediaItem.fromUri(videoUri));
            exoPlayer.prepare();
            exoPlayer.play();

            // Show Toast with current file name
            Toast.makeText(this, "Now Playing: " + videoFiles.get(index).getName(), Toast.LENGTH_SHORT).show();

            // Update Play/Pause button
            imgPlayPause.setImageResource(R.drawable.pause);

            // Update SeekBar
            exoPlayer.addListener(new com.google.android.exoplayer2.Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == ExoPlayer.STATE_READY) {
                        videoSeekbar.setMax((int) exoPlayer.getDuration());
                        updateTimeLabels();
                        updateSeekBar();
                    }
                }
            });

            currentVideoIndex = index;
        }
    }


    private void playNextVideo() {
        if (currentVideoIndex < videoFiles.size() - 1) {
            initializePlayer(++currentVideoIndex);
        }
    }

    private void playPreviousVideo() {
        if (currentVideoIndex > 0) {
            initializePlayer(--currentVideoIndex);
        }
    }

    private void togglePlayPause() {
        if (exoPlayer != null) {
            if (exoPlayer.isPlaying()) {
                exoPlayer.pause();
                imgPlayPause.setImageResource(R.drawable.play);
            } else {
                exoPlayer.play();
                imgPlayPause.setImageResource(R.drawable.pause);
                updateSeekBar();
            }
        }
    }

    private void updateSeekBar() {
        handler.postDelayed(() -> {
            if (exoPlayer != null && exoPlayer.isPlaying()) {
                videoSeekbar.setProgress((int) exoPlayer.getCurrentPosition());
                updateTimeLabels();
                updateSeekBar();
            }
        }, 1000);
    }

    private void updateTimeLabels() {
        if (exoPlayer != null) {
            int currentTime = (int) exoPlayer.getCurrentPosition();
            int totalTime = (int) exoPlayer.getDuration();
            tvStartTime.setText(formatTime(currentTime));
            tvEndTime.setText(formatTime(totalTime));
        }
    }

    private String formatTime(int millis) {
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
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
