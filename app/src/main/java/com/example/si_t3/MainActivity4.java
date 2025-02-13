package com.example.si_t3;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import android.database.Cursor;
import android.content.Context;

public class MainActivity4 extends AppCompatActivity {

    private static final int PICK_VIDEO_REQUEST = 1;
    private static final String TAG = "ConvertMp4ToMp3";

    private Uri videoUri;
    private String inputFilePath = "";
    private ProgressDialog progressDialog;
    private TextView txtFile;
    private Button btnConvert;
    private ProgressBar progressBar;

    private ActivityResultLauncher<String> pickVideoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        txtFile = findViewById(R.id.txtFile);
        btnConvert = findViewById(R.id.btnConvert);
        progressBar = findViewById(R.id.progressBar);

        requestStoragePermissions();

        findViewById(R.id.btnSelectVideo).setOnClickListener(v -> selectVideo());

        btnConvert.setOnClickListener(v -> {
            if (videoUri != null) {
                convertMp4ToMp3();
            } else {
                Toast.makeText(this, "Please select an MP4 file first!", Toast.LENGTH_SHORT).show();
            }
        });

        pickVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            videoUri = uri;
                            txtFile.setText("Selected: " + getFileName(MainActivity4.this, uri));
                            String fileName = getFileName(MainActivity4.this, videoUri);

                            inputFilePath = copyFileToAppDir(videoUri, fileName);
                            if (inputFilePath == null) {
                                Toast.makeText(MainActivity4.this, "File copy failed!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity4.this, "Invalid file selection!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, 1);
    }

    private void selectVideo() {
        pickVideoLauncher.launch("video/mp4");
    }

    private void convertMp4ToMp3() {
        String outputFilePath = getConvertedFilePath();

        File mergedFile = new File(outputFilePath);
        if (mergedFile.exists()) {
            mergedFile.delete();
        }

        runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            btnConvert.setEnabled(false);
        });

        String command = "-i \"" + inputFilePath + "\" -q:a 0 -map a \"" + outputFilePath + "\"";

        FFmpegKit.executeAsync(command, session -> {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                btnConvert.setEnabled(true);

                if (ReturnCode.isSuccess(session.getReturnCode())) {
                    Toast.makeText(this, "Converted Successfully! Saved at:\n" + outputFilePath, Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "FFmpeg Failed: " + session.getOutput());
                    Log.e(TAG, "Error Trace: " + session.getFailStackTrace());
                    Toast.makeText(this, "Conversion Failed!", Toast.LENGTH_SHORT).show();
                }
            });
        });
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
