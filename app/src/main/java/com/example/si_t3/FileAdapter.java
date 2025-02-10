package com.example.si_t3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.si_t3.FileItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private final List<FileItem> fileList;

    public FileAdapter(List<FileItem> fileList) {
        this.fileList = fileList;
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
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileName, folderName, fileDate;

        ViewHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            folderName = itemView.findViewById(R.id.folderName);
            fileDate = itemView.findViewById(R.id.fileDate);
        }
    }

    public void updateList(List<FileItem> newList) {
        this.fileList.clear();
        this.fileList.addAll(newList);
        notifyDataSetChanged();
    }

}
