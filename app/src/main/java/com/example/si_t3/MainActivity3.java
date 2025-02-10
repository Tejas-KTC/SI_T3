package com.example.si_t3;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.example.si_t3.CustomRangeBar;
import com.example.si_t3.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity3 extends AppCompatActivity {

    private static final int PICK_AUDIO_REQUEST = 1;
    private static final String TAG = "TrimAudio";

    private Uri audioUri;
    private TextView txtFile, txtStart, txtEnd;
    private ProgressBar progressBar;
    private Button btnTrim;
    private float startTime = 0;
    private float endTime = 30;
    private float audioDuration = 0;
    private String inputFilePath = "";
    private ProgressDialog progressDialog;
    private CustomRangeBar rangeSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        txtFile = findViewById(R.id.txtFile);
        txtStart = findViewById(R.id.txtStart);
        txtEnd = findViewById(R.id.txtEnd);
        progressBar = findViewById(R.id.progressBar);
        btnTrim = findViewById(R.id.btnTrim);
        rangeSlider = findViewById(R.id.rangeBar);

        requestStoragePermissions();

        findViewById(R.id.btnSelect).setOnClickListener(v -> selectAudio());

        rangeSlider.setOnRangeChangedListener((start, end) -> {
            startTime = start;
            endTime = end;
            txtStart.setText("Start: " + formatTime(startTime));
            txtEnd.setText("End: " + formatTime(endTime));
        });

        btnTrim.setOnClickListener(v -> {
            if (audioUri != null) {
                trimAudioFile();
            } else {
                Toast.makeText(this, "Please select an audio file!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        }
    }

    private void selectAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            audioUri = data.getData();
            String fileName = getFileName(this, audioUri);
            txtFile.setText("Selected: " + fileName);

            inputFilePath = copyFileToAppDir(audioUri, fileName);
            if (inputFilePath != null) {
                audioDuration = getAudioDuration(inputFilePath);
                rangeSlider.setTimeRange(0, ((int) audioDuration));
                txtEnd.setText("End: " + formatTime(audioDuration));
            } else {
                Toast.makeText(this, "File copy failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void trimAudioFile() {
        String outputFile = getTrimmedOutputFilePath();

        runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            btnTrim.setEnabled(false);
        });

        String command = "-i \"" + inputFilePath + "\" -ss " + startTime + " -to " + endTime + " -c copy \"" + outputFile + "\"";

        FFmpegKit.executeAsync(command, session -> {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                btnTrim.setEnabled(true);

                if (ReturnCode.isSuccess(session.getReturnCode())) {
                    Toast.makeText(this, "Trimmed Successfully! Saved at:\n" + outputFile, Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "FFmpeg Failed: " + session.getOutput());
                    Log.e(TAG, "Error Trace: " + session.getFailStackTrace());
                    Toast.makeText(this, "Failed to Trim audio!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String getTrimmedOutputFilePath() {
        File outputDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "TrimmedAudio");
        if (!outputDir.exists()) outputDir.mkdirs();

        File outputFile = new File(outputDir, "trimmed_audio2.mp3");

        if (outputFile.exists()) {
            outputFile.delete();
        }

        return outputFile.getAbsolutePath();
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
        File outputFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), fileName);
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

    private float getAudioDuration(String filePath) {
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(filePath);
            player.prepare();
            return player.getDuration() / 1000f;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        } finally {
            player.release();
        }
    }

    private String formatTime(float seconds) {
        return String.format("%02d:%02d", (int) (seconds / 60), (int) (seconds % 60));
    }
}
