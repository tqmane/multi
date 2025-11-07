package com.tqmane.multiwindowpatch;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * マルチウィンドウアクションレシーバー
 * 通知からマルチウィンドウモードを開くためのBroadcastReceiver
 */
public class MultiWindowActionReceiver extends BroadcastReceiver {
    
    private static final String TAG = "MultiWindowPatch";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (MultiWindowNotificationService.ACTION_OPEN_MULTI_WINDOW.equals(action)) {
            String packageName = intent.getStringExtra(
                MultiWindowNotificationService.EXTRA_PACKAGE_NAME
            );
            
            if (packageName != null) {
                openInMultiWindow(context, packageName);
            }
        }
    }
    
    /**
     * 指定されたパッケージをマルチウィンドウモードで開く
     */
    private void openInMultiWindow(Context context, String packageName) {
        try {
            // パッケージマネージャーからランチャーインテントを取得
            Intent launchIntent = context.getPackageManager()
                .getLaunchIntentForPackage(packageName);
            
            if (launchIntent == null) {
                XposedBridge.log(TAG + ": Launch intent not found for " + packageName);
                return;
            }
            
            // マルチウィンドウモードでアプリを起動
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // フラグを設定
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
                
                // ActivityOptionsでマルチウィンドウモードを指定
                ActivityOptions options = ActivityOptions.makeBasic();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // 分割画面モードで起動
                    options.setLaunchBounds(null);  // システムにバウンドを決定させる
                }
                
                context.startActivity(launchIntent, options.toBundle());
                
                XposedBridge.log(TAG + ": Launched " + packageName + " in multi-window mode");
            } else {
                // Android 7.0未満では通常起動
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
            }
            
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to launch in multi-window: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
