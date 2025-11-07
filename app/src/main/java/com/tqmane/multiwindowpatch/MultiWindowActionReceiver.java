package com.tqmane.multiwindowpatch;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.robv.android.xposed.XposedBridge;

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
            XposedBridge.log(TAG + ": Attempting to launch " + packageName + " in multi-window mode");
            
            // パッケージマネージャーからランチャーインテントを取得
            Intent launchIntent = context.getPackageManager()
                .getLaunchIntentForPackage(packageName);
            
            if (launchIntent == null) {
                XposedBridge.log(TAG + ": Launch intent not found for " + packageName);
                return;
            }
            
            // Android 15+ でマルチウィンドウモードでアプリを起動
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
            
            // ActivityOptionsでマルチウィンドウモードを指定
            ActivityOptions options = ActivityOptions.makeBasic();
            options.setLaunchBounds(null);  // システムにバウンドを決定させる
            
            context.startActivity(launchIntent, options.toBundle());
            XposedBridge.log(TAG + ": Successfully launched " + packageName + " in multi-window mode");
            
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to launch in multi-window: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
