package com.tqmane.multiwindowpatch;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * メインアクティビティ
 * インストール済みアプリ一覧を表示し、選択したアプリをマルチウィンドウモードで起動
 */
public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MultiWindowPatch";
    
    private RecyclerView recyclerView;
    private EditText searchBox;
    private AppListAdapter adapter;
    private List<AppInfo> allApps;
    private List<AppInfo> filteredApps;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        recyclerView = findViewById(R.id.recyclerView);
        searchBox = findViewById(R.id.searchBox);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // アプリ一覧を読み込み
        loadInstalledApps();
        
        // アダプターをセット
        adapter = new AppListAdapter(this, filteredApps, this::launchAppInMultiWindow);
        recyclerView.setAdapter(adapter);
        
        // 検索機能
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    /**
     * インストール済みアプリを読み込む
     */
    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        allApps = new ArrayList<>();
        
        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName = resolveInfo.activityInfo.packageName;
            String appName = resolveInfo.loadLabel(pm).toString();
            
            // システムアプリや自分自身を除外
            if (packageName.equals(getPackageName())) {
                continue;
            }
            
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                allApps.add(new AppInfo(
                    packageName,
                    appName,
                    resolveInfo.loadIcon(pm),
                    (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                ));
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "Package not found: " + packageName);
            }
        }
        
        // アプリ名でソート
        Collections.sort(allApps, (a, b) -> a.appName.compareToIgnoreCase(b.appName));
        
        filteredApps = new ArrayList<>(allApps);
    }
    
    /**
     * アプリをフィルタリング
     */
    private void filterApps(String query) {
        filteredApps.clear();
        
        if (query.isEmpty()) {
            filteredApps.addAll(allApps);
        } else {
            String lowerQuery = query.toLowerCase();
            for (AppInfo app : allApps) {
                if (app.appName.toLowerCase().contains(lowerQuery) ||
                    app.packageName.toLowerCase().contains(lowerQuery)) {
                    filteredApps.add(app);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
    }
    
    /**
     * アプリをマルチウィンドウモードで起動
     */
    private void launchAppInMultiWindow(AppInfo appInfo) {
        try {
            PackageManager pm = getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName);
            
            if (launchIntent == null) {
                Toast.makeText(this, "アプリを起動できません", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Android 15+ でマルチウィンドウモードで起動
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
            
            ActivityOptions options = ActivityOptions.makeBasic();
            options.setLaunchBounds(null);
            startActivity(launchIntent, options.toBundle());
            
            Toast.makeText(this, 
                appInfo.appName + " をマルチウィンドウで起動しました", 
                Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch app: " + e.getMessage());
            Toast.makeText(this, "起動に失敗しました: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }
    }
    }
}
