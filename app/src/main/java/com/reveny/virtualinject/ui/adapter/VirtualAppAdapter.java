package com.reveny.virtualinject.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.reveny.virtualinject.R;
import com.reveny.virtualinject.model.VirtualApp;

import java.util.List;

public class VirtualAppAdapter extends RecyclerView.Adapter<VirtualAppAdapter.ViewHolder> {

    private List<VirtualApp> appList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(VirtualApp app);
        void onItemLongClick(VirtualApp app);
    }

    public VirtualAppAdapter(List<VirtualApp> appList, OnItemClickListener listener) {
        this.appList = appList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_virtual_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VirtualApp app = appList.get(position);
        holder.appLabel.setText(app.getLabel());
        holder.appIcon.setImageDrawable(app.getIcon());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(app));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(app);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appLabel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.app_icon);
            appLabel = itemView.findViewById(R.id.app_label);
        }
    }
}
