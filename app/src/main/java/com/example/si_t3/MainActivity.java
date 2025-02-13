package com.example.si_t3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button btnMergeAudio, btnTrimAudio, btnMp4toMp3, btnVideoTrim, btnList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*m4a = audio/x-m4a
        aac = audio/aac
        mp3 = audio/mpeg*/

        btnMergeAudio = findViewById(R.id.btnMergeAudio);
        btnTrimAudio = findViewById(R.id.btnTrimAudio);
        btnMp4toMp3 = findViewById(R.id.btnMp4toMP3);
        btnList = findViewById(R.id.btnList);
        btnVideoTrim = findViewById(R.id.btnVideoTrim);

        btnMergeAudio.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MainActivity2.class)));
        btnTrimAudio.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MainActivity3.class)));
        btnMp4toMp3.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MainActivity4.class)));
        btnList.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MainActivity5.class)));
    }
}