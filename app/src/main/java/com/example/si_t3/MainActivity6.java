package com.example.si_t3;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class MainActivity6 extends AppCompatActivity {

    private FrameSeekBar seekBar;
    private TextView tvTime;
    Button btnSelect;
    private Uri videoUri;
    private String inputFilePath = "";
    private ActivityResultLauncher<String> pickVideoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);

        seekBar = findViewById(R.id.frameSeekBar);
        tvTime = findViewById(R.id.tvTime);

        requestStoragePermissions();

        findViewById(R.id.btnSelect).setOnClickListener(v -> selectVideo());


        seekBar.setTimeListener((startMs, endMs) -> {
            String timeText = String.format("%s - %s",
                    formatDuration(startMs),
                    formatDuration(endMs)
            );
            tvTime.setText(timeText);
        });

        pickVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            videoUri = uri;
                            String fileName = getFileName(MainActivity6.this, videoUri);

                            inputFilePath = copyFileToAppDir(videoUri, fileName);
                            if (inputFilePath == null) {
                                Toast.makeText(MainActivity6.this, "File copy failed!", Toast.LENGTH_SHORT).show();
                            }
                            seekBar.setVideoData(inputFilePath, 25);
                        } else {
                            Toast.makeText(MainActivity6.this, "Invalid file selection!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private String formatDuration(long millis) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        );
    }

    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, 1);
    }

    private void selectVideo() {
        pickVideoLauncher.launch("video/mp4");
    }

    private String getConvertedFilePath() {
        File outputDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "ConvertedAudio");
        if (!outputDir.exists()) outputDir.mkdirs();
        return new File(outputDir, "converted_audio.mp3").getAbsolutePath();
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