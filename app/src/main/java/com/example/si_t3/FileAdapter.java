package com.example.si_t3;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
    private Context context;
    private List<FileItem> fileList;
    private OnFileDeletedListener listener;

    private final List<FileItem> selectedItems = new ArrayList<>();


    public interface OnFileDeletedListener {
        void onFileDeleted(FileItem fileItem);
    }

    public FileAdapter(Context context, List<FileItem> fileList, OnFileDeletedListener listener) {
        this.context = context;
        this.fileList = fileList;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem fileItem = fileList.get(position);
        holder.fileName.setText(fileItem.getFileName());
        holder.folderName.setText("Folder: " + fileItem.getFolderName());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.fileDate.setText("Modified: " + sdf.format(fileItem.getLastModified()));

        // Change background color based on selection
        if (selectedItems.contains(fileItem)) {
            holder.itemView.setBackgroundColor(Color.LTGRAY); // Highlight selected item
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.btnDelete.setVisibility(View.GONE);
        }

        // Long press to select/deselect item
        holder.itemView.setOnLongClickListener(v -> {
            toggleSelection(fileItem, holder);
            return true;
        });

        // Click delete button to remove the file
        holder.btnDelete.setOnClickListener(v -> {
            if (selectedItems.contains(fileItem)) {
                deleteFile(fileItem);
                selectedItems.remove(fileItem);
                notifyDataSetChanged();
                listener.onFileDeleted(fileItem);
            }
        });

    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileName, folderName, fileDate;
        ImageView btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            folderName = itemView.findViewById(R.id.folderName);
            fileDate = itemView.findViewById(R.id.fileDate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private void toggleSelection(FileItem fileItem, ViewHolder holder) {
        if (selectedItems.contains(fileItem)) {
            selectedItems.remove(fileItem);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            selectedItems.add(fileItem);
            holder.itemView.setBackgroundColor(Color.LTGRAY);
            holder.btnDelete.setVisibility(View.VISIBLE);
        }
    }

    private void deleteFile(FileItem fileItem) {
        File file = new File(fileItem.getFilePath());
        if (file.exists() && file.delete()) {
            Toast.makeText(context, "File deleted: " + fileItem.getFileName(), Toast.LENGTH_SHORT).show();
            fileList.remove(fileItem);
        } else {
            Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateList(List<FileItem> newList) {
        fileList.clear();
        fileList.addAll(newList);
        notifyDataSetChanged();
    }
}
