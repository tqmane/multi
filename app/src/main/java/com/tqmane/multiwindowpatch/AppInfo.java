package com.tqmane.multiwindowpatch;

import android.graphics.drawable.Drawable;

/**
 * アプリ情報を保持するデータクラス
 */
public class AppInfo {
    public String packageName;
    public String appName;
    public Drawable icon;
    public boolean isSystemApp;
    
    public AppInfo(String packageName, String appName, Drawable icon, boolean isSystemApp) {
        this.packageName = packageName;
        this.appName = appName;
        this.icon = icon;
        this.isSystemApp = isSystemApp;
    }
}
