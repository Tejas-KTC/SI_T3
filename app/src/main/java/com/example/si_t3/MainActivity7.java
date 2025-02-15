package com.example.si_t3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity7 extends AppCompatActivity {
    private ExoPlayer player;
    private PlayerView playerView;
    private SeekBar trimSeekBar;
    private View startThumb, endThumb;
    private LinearLayout frameContainer;
    private ProgressBar progressBar;
    private long videoDuration = 0;
    private long startTime = 0, endTime = 0;
    private Uri videoUri;
    private String inputFilePath = "";

    private TextView tvStart, tvEnd;
    private Button btnSelect, btnDone;
    private ActivityResultLauncher<String> pickVideoLauncher;
    private ImageView btnPlayPause;
    private Boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main7);

        playerView = findViewById(R.id.playerView);
        trimSeekBar = findViewById(R.id.trimSeekBar);
        startThumb = findViewById(R.id.startThumb);
        endThumb = findViewById(R.id.endThumb);
        frameContainer = findViewById(R.id.frameContainer);
        btnSelect = findViewById(R.id.btnSelect);
        tvStart = findViewById(R.id.startTime);
        tvEnd = findViewById(R.id.endTime);
        progressBar = findViewById(R.id.progressBar);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnDone = findViewById(R.id.btnDone);

        playerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        setupRangeBar();
        requestStoragePermissions();

        // Register video picker
        pickVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            videoUri = uri;
                            String fileName = getFileName(MainActivity7.this, videoUri);
                            inputFilePath = copyFileToAppDir(videoUri, fileName);

                            if (inputFilePath != null) {
                                extractFrames(inputFilePath);
                            } else {
                                Toast.makeText(MainActivity7.this, "File copy failed!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity7.this, "Invalid file selection!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        btnSelect.setOnClickListener(v -> pickVideoLauncher.launch("video/*"));

        btnPlayPause.setOnClickListener(v -> {
            if (player != null) {
                if (player.isPlaying()) {
                    player.pause();
                    btnPlayPause.setImageResource(R.drawable.play); // Change to play icon
                } else {
                    player.play();
                    btnPlayPause.setImageResource(R.drawable.pause); // Change to pause icon
                }
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputFilePath != null){
                    trimVideo();
                }
            }
        });

    }

    private void trimVideo() {
        if (videoUri == null || inputFilePath.isEmpty()) {
            Toast.makeText(this, "No video selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        File outputDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "TrimmedVideo");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String outputFilePath = new File(outputDir, "trimmed_" + System.currentTimeMillis() + ".mp4").getAbsolutePath();

        String start = formatTime(startTime);
        String duration = formatTime(endTime - startTime);

        String cmd = "-i " + inputFilePath + " -ss " + start + " -t " + duration + " -c copy " + outputFilePath;

        progressBar.setVisibility(View.VISIBLE);

        FFmpegKit.executeAsync(cmd, session -> {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (ReturnCode.isSuccess(session.getReturnCode())) {
                    Toast.makeText(this, "Video trimmed successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Trim failed!", Toast.LENGTH_SHORT).show();
                }
            });


        });

    }


    private void setupExoPlayer(String videoPath) {
        if (player != null) {
            player.release();
        }

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(videoPath);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(false);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    videoDuration = player.getDuration();
                    trimSeekBar.setMax((int) videoDuration);
                    endTime = videoDuration;

                    if(isFirst) {
                        tvStart.setText(formatTime(startTime));
                        tvEnd.setText(formatTime(endTime));
                        isFirst = false;
                    }

                    playerView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    btnPlayPause.setImageResource(R.drawable.pause);
                } else {
                    btnPlayPause.setImageResource(R.drawable.play);
                }
            }
        });

    }

    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) ((millis / 1000) / 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void extractFrames(String videoPath) {
        progressBar.setVisibility(View.VISIBLE);
        frameContainer.removeAllViews();
        playerView.setVisibility(View.GONE);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(videoPath);

                long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                int frameCount = 10;
                long interval = duration / frameCount;

                for (int i = 0; i < frameCount; i++) {
                    long timeUs = i * interval * 1000;
                    Bitmap frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST);
                    if (frame != null) {
                        Bitmap scaledFrame = Bitmap.createScaledBitmap(frame, 100, 160, false);
                        runOnUiThread(() -> addFrameToView(scaledFrame));
                    }
                }
                retriever.release();

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    setupExoPlayer(videoPath);
                    if (player != null) {
                        player.setPlayWhenReady(true);
                    }

                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void addFrameToView(Bitmap frame) {
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(frame);
        frameContainer.addView(imageView);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupRangeBar() {
        trimSeekBar.post(() -> {
            int seekBarWidth = trimSeekBar.getWidth();
            int thumbWidth = startThumb.getWidth();
            float margin = getResources().getDisplayMetrics().density * 8;

            startThumb.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    float newX = event.getRawX() - trimSeekBar.getLeft() - (thumbWidth / 2);
                    newX = Math.max(margin, Math.min(newX, endThumb.getX() - thumbWidth - margin));
                    startThumb.setX(newX);

                    startTime = (long) (videoDuration * (newX / (seekBarWidth - thumbWidth)));
                    player.seekTo(startTime);
                    tvStart.setText(formatTime(startTime));
                }
                return true;
            });

            endThumb.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    float newX = event.getRawX() - trimSeekBar.getLeft() - (thumbWidth / 2);
                    newX = Math.max(startThumb.getX() + thumbWidth + margin, Math.min(newX, seekBarWidth - thumbWidth - margin));
                    endThumb.setX(newX);

                    endTime = (long) (videoDuration * (newX / (seekBarWidth - thumbWidth)));
                    tvEnd.setText(formatTime(endTime));
                }
                return true;
            });
        });
    }

    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, 1);
    }

    private String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        return result != null ? result : uri.getLastPathSegment();
    }

    private String copyFileToAppDir(Uri uri, String fileName) {
        File outputFile = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), fileName);
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
