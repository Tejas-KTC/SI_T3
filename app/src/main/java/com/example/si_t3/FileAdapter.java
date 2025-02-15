package com.example.si_t3;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.si_t3.FileItem;
import com.example.si_t3.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private List<FileItem> fileList;
    private OnItemClickListener listener;
    private boolean isSelectionMode = false;

    public interface OnItemClickListener {
        void onItemClick(FileItem fileItem);
        void onItemLongClick(FileItem fileItem, int position);
        void onDeleteClick(FileItem fileItem, int position);
    }

    public FileAdapter(List<FileItem> fileList, OnItemClickListener listener) {
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
        FileItem item = fileList.get(position);

        // Check if it's a folder (empty parentFolder means it's a folder)
        boolean isFolder = item.getParentFolder().isEmpty();

        // If it's a folder, show only the name and hide other fields
        if (isFolder) {
            holder.fileName.setText(item.getName());
            holder.fileParentFolder.setVisibility(View.GONE);
            holder.fileCreationTime.setVisibility(View.GONE);
            holder.lnlDelcan.setVisibility(View.GONE); // Hide delete/cancel options
        } else {
            // It's a file, so display full details
            holder.fileName.setText(item.getName());
            holder.fileParentFolder.setText("Folder: " + item.getParentFolder());
            holder.fileParentFolder.setVisibility(View.VISIBLE);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            holder.fileCreationTime.setText("Created: " + sdf.format(new Date(item.getCreationTimeMillis())));
            holder.fileCreationTime.setVisibility(View.VISIBLE);

            holder.lnlDelcan.setVisibility(item.isSelected() ? View.VISIBLE : View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));

        if (!isFolder) {
            holder.itemView.setOnLongClickListener(v -> {
                listener.onItemLongClick(item, position);
                return true;
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(item, position));

        holder.btnCancel.setOnClickListener(v -> {
            item.setSelected(false);
            notifyDataSetChanged();
        });
    }


    @Override
    public int getItemCount() {
        return fileList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileName, fileCreationTime, fileParentFolder;
        LinearLayout lnlDelcan;
        Button btnDelete, btnCancel;

        ViewHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            fileCreationTime = itemView.findViewById(R.id.fileDate);
            fileParentFolder = itemView.findViewById(R.id.folderName);
            lnlDelcan = itemView.findViewById(R.id.lnlDelCan);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }

    public void selectAll(boolean select) {
        for (FileItem item : fileList) {
            item.setSelected(select);
        }
        notifyDataSetChanged();
    }
}
