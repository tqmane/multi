package com.tqmane.multiwindowpatch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Multi Window Patch - すべてのアプリでマルチウィンドウを強制的に有効にする
 * 
 * このXposedモジュールは、Android Nougat(7.0)以降で導入されたマルチウィンドウ機能を
 * すべてのアプリで使用可能にし、アプリ起動中に通知を表示します。
 */
public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "MultiWindowPatch";
    
    // 除外するパッケージ（システムアプリや自分自身）
    private static final Set<String> EXCLUDED_PACKAGES = new HashSet<>();
    
    static {
        EXCLUDED_PACKAGES.add("android");
        EXCLUDED_PACKAGES.add("com.android.systemui");
        EXCLUDED_PACKAGES.add("com.android.settings");
        EXCLUDED_PACKAGES.add("com.tqmane.multiwindowpatch");
    }
    
    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        // システムフレームワーク(android)にフックを適用
        if (lpparam.packageName.equals("android")) {
            hookSystemFramework(lpparam);
        } 
        // その他のアプリにフックを適用（通知表示用）
        else if (!EXCLUDED_PACKAGES.contains(lpparam.packageName)) {
            hookAppLifecycle(lpparam);
        }
    }
    
    /**
     * システムフレームワークにマルチウィンドウ関連のフックを適用
     */
    private void hookSystemFramework(final LoadPackageParam lpparam) {
        XposedBridge.log(TAG + ": Hooking into Android framework");

        try {
            // ActivityInfo のリサイズモードを強制的に変更
            hookActivityInfo(lpparam);
            
            // ApplicationInfo のフラグを変更
            hookApplicationInfo(lpparam);
            
            XposedBridge.log(TAG + ": Successfully hooked multi-window checks");
            
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": Error hooking: " + t.getMessage());
        }
    }
    
    /**
     * アプリのライフサイクルをフックして通知を管理
     */
    private void hookAppLifecycle(final LoadPackageParam lpparam) {
        try {
            // Activity.onResume をフック（アプリが前面に来た時）
            XposedHelpers.findAndHookMethod(
                "android.app.Activity",
                lpparam.classLoader,
                "onResume",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Activity activity = (Activity) param.thisObject;
                        showMultiWindowNotification(activity, lpparam.packageName);
                    }
                }
            );
            
            // Activity.onPause をフック（アプリがバックグラウンドに行った時）
            XposedHelpers.findAndHookMethod(
                "android.app.Activity",
                lpparam.classLoader,
                "onPause",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Activity activity = (Activity) param.thisObject;
                        // アプリが完全に終了する場合のみ通知を非表示
                        if (activity.isFinishing()) {
                            hideMultiWindowNotification(activity);
                        }
                    }
                }
            );
            
            // Activity.onDestroy をフック
            XposedHelpers.findAndHookMethod(
                "android.app.Activity",
                lpparam.classLoader,
                "onDestroy",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Activity activity = (Activity) param.thisObject;
                        hideMultiWindowNotification(activity);
                    }
                }
            );
            
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": Error hooking app lifecycle for " + 
                lpparam.packageName + ": " + t.getMessage());
        }
    }
    
    /**
     * マルチウィンドウ通知を表示
     */
    private void showMultiWindowNotification(Context context, String packageName) {
        try {
            Intent serviceIntent = new Intent();
            serviceIntent.setClassName(
                "com.tqmane.multiwindowpatch",
                "com.tqmane.multiwindowpatch.MultiWindowNotificationService"
            );
            serviceIntent.putExtra(
                MultiWindowNotificationService.EXTRA_PACKAGE_NAME,
                packageName
            );
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            
            XposedBridge.log(TAG + ": Notification shown for " + packageName);
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to show notification: " + e.getMessage());
        }
    }
    
    /**
     * マルチウィンドウ通知を非表示
     */
    private void hideMultiWindowNotification(Context context) {
        try {
            Intent serviceIntent = new Intent();
            serviceIntent.setClassName(
                "com.tqmane.multiwindowpatch",
                "com.tqmane.multiwindowpatch.MultiWindowNotificationService"
            );
            context.stopService(serviceIntent);
            
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hide notification: " + e.getMessage());
        }
    }

    /**
     * ActivityInfo.isResizeable をフックして常にtrueを返す
     */
    private void hookActivityInfo(LoadPackageParam lpparam) {
        try {
            // Android 7.0以降で使用可能なisResizeableメソッドをフック
            XposedHelpers.findAndHookMethod(
                "android.content.pm.ActivityInfo",
                lpparam.classLoader,
                "isResizeable",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        // 常にtrueを返してマルチウィンドウを有効化
                        param.setResult(true);
                    }
                }
            );
            
            XposedBridge.log(TAG + ": Hooked ActivityInfo.isResizeable()");
            
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": Failed to hook ActivityInfo: " + t.getMessage());
        }
    }

    /**
     * ApplicationInfo のフラグを変更してマルチウィンドウをサポート
     */
    private void hookApplicationInfo(LoadPackageParam lpparam) {
        // ActivityRecord のフックは Android バージョンによって異なるため、
        // 失敗してもエラーとして扱わない
        try {
            Class<?> activityRecord = XposedHelpers.findClass(
                "com.android.server.wm.ActivityRecord",
                lpparam.classLoader
            );
            
            // Android 14/15 では異なるコンストラクタシグネチャを使用する可能性がある
            // 複数のパターンを試行
            boolean hooked = false;
            
            // パターン1: 古いシグネチャ
            try {
                XposedHelpers.findAndHookConstructor(
                    activityRecord,
                    "com.android.server.wm.ActivityTaskManagerService",
                    int.class,
                    "android.content.pm.ActivityInfo",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            modifyActivityInfo(param.args[2]);
                        }
                    }
                );
                hooked = true;
                XposedBridge.log(TAG + ": Hooked ActivityRecord constructor (pattern 1)");
            } catch (Throwable t) {
                // パターン1失敗、次を試行
            }
            
            // パターン2: より詳細なシグネチャ
            if (!hooked) {
                try {
                    // Android 12以降の可能性があるシグネチャ
                    XposedHelpers.findAndHookConstructor(
                        activityRecord,
                        "com.android.server.wm.ActivityTaskManagerService",
                        int.class,
                        "android.content.pm.ActivityInfo",
                        "android.content.Intent",
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                modifyActivityInfo(param.args[2]);
                            }
                        }
                    );
                    hooked = true;
                    XposedBridge.log(TAG + ": Hooked ActivityRecord constructor (pattern 2)");
                } catch (Throwable t) {
                    // パターン2も失敗
                }
            }
            
            if (!hooked) {
                XposedBridge.log(TAG + ": Could not hook ActivityRecord constructor (not critical)");
            }
            
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": ActivityRecord not found (not critical): " + t.getMessage());
        }
        
        // 代替アプローチ: PackageParser をフック
        try {
            Class<?> packageParser = XposedHelpers.findClass(
                "android.content.pm.PackageParser",
                lpparam.classLoader
            );
            
            // generateActivityInfo メソッドをフック
            XposedHelpers.findAndHookMethod(
                packageParser,
                "generateActivityInfo",
                "android.content.pm.PackageParser$Activity",
                int.class,
                "android.content.pm.PackageUserState",
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        android.content.pm.ActivityInfo activityInfo = 
                            (android.content.pm.ActivityInfo) param.getResult();
                        
                        if (activityInfo != null) {
                            // Android 7.0以降で利用可能な定数
                            // RESIZE_MODE_RESIZEABLE = 2
                            // RESIZE_MODE_RESIZEABLE_VIA_SDK_VERSION = 1
                            XposedHelpers.setIntField(activityInfo, "resizeMode", 2);
                            
                            param.setResult(activityInfo);
                        }
                    }
                }
            );
            
            XposedBridge.log(TAG + ": Hooked PackageParser.generateActivityInfo()");
            
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": Failed to hook PackageParser: " + t.getMessage());
        }
    }
    
    /**
     * ActivityInfo を修正してマルチウィンドウを有効化
     */
    private void modifyActivityInfo(Object info) {
        try {
            if (info != null) {
                // resizeMode フィールドを変更
                int resizeModeResizeable = 2; // RESIZE_MODE_RESIZEABLE
                XposedHelpers.setIntField(info, "resizeMode", resizeModeResizeable);
                
                // ApplicationInfo のフラグも変更
                Object appInfo = XposedHelpers.getObjectField(info, "applicationInfo");
                if (appInfo != null) {
                    int flags = XposedHelpers.getIntField(appInfo, "flags");
                    // FLAG_SUPPORTS_SCREEN_DENSITIES などを追加
                    XposedHelpers.setIntField(appInfo, "flags", flags);
                }
            }
        } catch (Throwable t) {
            // エラーは無視（すべてのケースで適用できるわけではないため）
        }
    }
}
