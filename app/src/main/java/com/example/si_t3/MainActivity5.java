package com.example.si_t3;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.si_t3.FileAdapter;
import com.example.si_t3.FileItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity5 extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FileAdapter adapter;
    private List<FileItem> fileList = new ArrayList<>();
    private final List<String> allowedFolders = Arrays.asList("ConvertedAudio", "MergedAudio", "TrimmedAudio", "TrimmedVideo");
    private boolean isFolderView = true;
    private String currentFolderPath;
    Button btnSelect;
    private boolean isAllSelected = false;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);

        recyclerView = findViewById(R.id.recyclerView);
        btnSelect = findViewById(R.id.btnSelectAll);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if(isFolderView){
            btnSelect.setVisibility(GONE);
        }
        else {
            btnSelect.setVisibility(VISIBLE);
        }

        adapter = new FileAdapter(fileList, new FileAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FileItem fileItem) {
                if (fileItem.isFolder()) {
                    loadFiles(fileItem.getPath());
                } else {
                    openFile(fileItem);
                }
            }

            @Override
            public void onItemLongClick(FileItem fileItem, int position) {
                fileItem.setSelected(!fileItem.isSelected());
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onDeleteClick(FileItem fileItem, int position) {
                File file = new File(fileItem.getPath());
                if (file.delete()) {
                    fileList.remove(position);
                    adapter.notifyItemRemoved(position);
                }
            }
        });

        recyclerView.setAdapter(adapter);
        loadFolders();

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAllSelected = !isAllSelected;
                adapter.selectAll(isAllSelected);

                btnSelect.setText(isAllSelected ? "Deselect All" : "Select All");
            }
        });
    }

    private void loadFolders() {
        fileList.clear();
        String musicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
        for (String folder : allowedFolders) {
            File folderFile = new File(musicPath, folder);
            if (folderFile.exists() && folderFile.isDirectory()) {
                fileList.add(new FileItem(folder, folderFile.getAbsolutePath(), true, "", 0));
            }
        }
        isFolderView = true;
        adapter.notifyDataSetChanged();
    }

    private void loadFiles(String folderPath) {
        fileList.clear();
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                fileList.add(new FileItem(file.getName(), file.getAbsolutePath(), false, folder.getName(), file.lastModified()));
            }
        }
        Collections.sort(fileList, (f1, f2) -> Long.compare(f2.getCreationTimeMillis(), f1.getCreationTimeMillis()));
        isFolderView = false;
        btnSelect.setVisibility(VISIBLE);
        currentFolderPath = folderPath;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (!isFolderView) {
            btnSelect.setVisibility(GONE);
            loadFolders();
        } else {
            super.onBackPressed();
        }
    }

    private void openFile(FileItem fileItem) {
        if (fileItem.getName().endsWith(".mp4")) {
            Intent intent = new Intent(this, MainActivity6.class);
            intent.putExtra("filePath", fileItem.getPath());
            intent.putExtra("fileName", fileItem.getName());
            intent.putExtra("folderName", fileItem.getParentFolder());
            startActivity(intent);
        } else if (fileItem.getName().endsWith(".mp3")) {
            LinearLayout lnlMp3 = findViewById(R.id.linearLayout);
            TextView txtFile = findViewById(R.id.txtFile);
            ImageView imgPlayPause = findViewById(R.id.btnPlayPause);
            TextView btnClose = findViewById(R.id.btnClose);

            String filePath = fileItem.getPath();
            txtFile.setText(fileItem.getName());

            lnlMp3.setVisibility(View.VISIBLE);

            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(filePath);
                mediaPlayer.prepare();
                mediaPlayer.start();
                isPlaying = true;
                imgPlayPause.setImageResource(R.drawable.pause);
            } catch (IOException e) {
                e.printStackTrace();
            }

            imgPlayPause.setOnClickListener(v -> {
                if (isPlaying) {
                    mediaPlayer.pause();
                    imgPlayPause.setImageResource(R.drawable.play);
                } else {
                    mediaPlayer.start();
                    imgPlayPause.setImageResource(R.drawable.pause);
                }
                isPlaying = !isPlaying;
            });

            btnClose.setOnClickListener(v -> {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    isPlaying = false;
                }
                lnlMp3.setVisibility(View.GONE);
            });
        }
    }
}
