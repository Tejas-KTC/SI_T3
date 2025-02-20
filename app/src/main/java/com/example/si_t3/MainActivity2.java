package com.example.si_t3;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.provider.OpenableColumns;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity2 extends AppCompatActivity {

    private static final int PICK_AUDIO_REQUEST_1 = 1;
    private static final int PICK_AUDIO_REQUEST_2 = 2;
    private static final String TAG = "AudioMerge";

    private Uri audioUri1, audioUri2;
    private TextView txtFile1, txtFile2;
    private ProgressBar progressBar;
    private Button btnMerge;
    private EditText edtFilename;
    String filename;


    private ActivityResultLauncher<String> pickAudioLauncher1;
    private ActivityResultLauncher<String> pickAudioLauncher2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        txtFile1 = findViewById(R.id.txtFile1);
        txtFile2 = findViewById(R.id.txtFile2);
        progressBar = findViewById(R.id.progressBar);
        btnMerge = findViewById(R.id.btnMerge);
        edtFilename = findViewById(R.id.edtFileName);


        // Allowed characters (A-Z, a-z, 0-9, _)
        String allowedChars = "[a-zA-Z0-9_]*";

        InputFilter inputFilter = (source, start, end, dest, dstart, dend) -> {
            if (source.toString().matches(allowedChars)) {
                return source; // Accept input
            } else {
                return ""; // Reject input
            }
        };

        // Apply the filter
        edtFilename.setFilters(new InputFilter[]{inputFilter});


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        }

        findViewById(R.id.btnSelect1).setOnClickListener(v -> selectAudio(PICK_AUDIO_REQUEST_1));
        findViewById(R.id.btnSelect2).setOnClickListener(v -> selectAudio(PICK_AUDIO_REQUEST_2));

        btnMerge.setOnClickListener(v -> {
            if (audioUri1 != null && audioUri2 != null && !edtFilename.getText().toString().isEmpty()) {
                mergeAudioFiles();
            } else {
                Toast.makeText(this, "Please select both audio files!", Toast.LENGTH_SHORT).show();
            }
        });

        pickAudioLauncher1 = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            audioUri1 = uri;
                            txtFile1.setText("Selected: " + getFileName(MainActivity2.this, uri));
                        } else {
                            Toast.makeText(MainActivity2.this, "Invalid file selection!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        pickAudioLauncher2 = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            audioUri2 = uri;
                            txtFile2.setText("Selected: " + getFileName(MainActivity2.this, uri));
                        } else {
                            Toast.makeText(MainActivity2.this, "Invalid file selection!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void selectAudio(int requestCode) {
        if (requestCode == PICK_AUDIO_REQUEST_1) {
            pickAudioLauncher1.launch("audio/mpeg");
        } else if (requestCode == PICK_AUDIO_REQUEST_2) {
            pickAudioLauncher2.launch("audio/mpeg");
        }
    }

    private void mergeAudioFiles() {
        String inputFile1 = copyFileToAppDir(audioUri1, "audio1.mp3");
        String inputFile2 = copyFileToAppDir(audioUri2, "audio2.mp3");

        if (inputFile1 == null || inputFile2 == null) {
            Toast.makeText(this, "Failed to copy files!", Toast.LENGTH_SHORT).show();
            return;
        }

        String outputFile = getOutputFilePath();

        File mergedFile = new File(outputFile);
        if (mergedFile.exists()) {
            mergedFile.delete();
        }

        runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            btnMerge.setEnabled(false);
        });

        String command = "-i " + inputFile1 + " -i " + inputFile2 +
                " -filter_complex \"[0:a][1:a]concat=n=2:v=0:a=1[out]\" -map \"[out]\" " + outputFile;

        FFmpegKit.executeAsync(command, session -> {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                btnMerge.setEnabled(true);

                if (ReturnCode.isSuccess(session.getReturnCode())) {
                    Toast.makeText(this, "Merged Successfully! Saved at:\n" + outputFile, Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "FFmpeg Failed: " + session.getOutput());
                    Log.e(TAG, "Error Trace: " + session.getFailStackTrace());
                    Toast.makeText(this, "Failed to merge audio!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }


    private String getOutputFilePath() {
        File outputDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "MergedAudio");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        return new File(outputDir, edtFilename.getText().toString()+".mp3").getAbsolutePath();
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
        File outputDir = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "temp_audio");
        if (!outputDir.exists()) outputDir.mkdirs();

        File outputFile = new File(outputDir, fileName);
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return outputFile.getAbsolutePath();
    }
}
