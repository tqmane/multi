package com.tqmane.multiwindowpatch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * アプリ一覧用のRecyclerViewアダプター
 */
public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {
    
    private Context context;
    private List<AppInfo> apps;
    private OnAppClickListener listener;
    
    public interface OnAppClickListener {
        void onAppClick(AppInfo appInfo);
    }
    
    public AppListAdapter(Context context, List<AppInfo> apps, OnAppClickListener listener) {
        this.context = context;
        this.apps = apps;
        this.listener = listener;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
            .inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppInfo app = apps.get(position);
        
        holder.appName.setText(app.appName);
        holder.packageName.setText(app.packageName);
        holder.appIcon.setImageDrawable(app.icon);
        
        // システムアプリの場合は薄く表示
        float alpha = app.isSystemApp ? 0.6f : 1.0f;
        holder.itemView.setAlpha(alpha);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAppClick(app);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return apps.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView packageName;
        
        ViewHolder(View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.appIcon);
            appName = itemView.findViewById(R.id.appName);
            packageName = itemView.findViewById(R.id.packageName);
        }
    }
}
