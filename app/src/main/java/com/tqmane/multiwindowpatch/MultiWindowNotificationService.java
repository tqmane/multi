package com.tqmane.multiwindowpatch;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

/**
 * マルチウィンドウ通知サービス
 * アプリ起動中に常時通知を表示し、マルチウィンドウモードへの切り替えボタンを提供
 */
public class MultiWindowNotificationService extends Service {
    
    private static final String CHANNEL_ID = "multi_window_channel";
    private static final String CHANNEL_NAME = "マルチウィンドウ";
    private static final int NOTIFICATION_ID = 1001;
    
    public static final String ACTION_OPEN_MULTI_WINDOW = "com.tqmane.multiwindowpatch.OPEN_MULTI_WINDOW";
    public static final String EXTRA_PACKAGE_NAME = "package_name";
    
    private String currentPackageName;
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(EXTRA_PACKAGE_NAME)) {
            currentPackageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
            showNotification(currentPackageName);
        }
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    /**
     * 通知チャンネルを作成（Android 8.0以降）
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW  // サイレント通知
            );
            channel.setDescription("マルチウィンドウモードへの切り替え通知");
            channel.setShowBadge(false);
            channel.setSound(null, null);  // 音なし
            channel.enableVibration(false);  // 振動なし
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * マルチウィンドウ切り替え通知を表示
     */
    private void showNotification(String packageName) {
        NotificationManager notificationManager = 
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager == null) {
            return;
        }
        
        // マルチウィンドウを開くインテント
        Intent openIntent = new Intent(ACTION_OPEN_MULTI_WINDOW);
        openIntent.setPackage(getPackageName());  // 明示的にパッケージを指定
        openIntent.putExtra(EXTRA_PACKAGE_NAME, packageName);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            openIntent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M 
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        // 通知を構築
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        
        builder.setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("マルチウィンドウ利用可能")
            .setContentText("タップしてマルチウィンドウモードを開く")
            .setOngoing(true)  // スワイプで消せないようにする
            .setShowWhen(false)
            .setContentIntent(pendingIntent);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPriority(Notification.PRIORITY_LOW);
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            builder.addAction(
                android.R.drawable.ic_menu_always_landscape_portrait,
                "マルチウィンドウで開く",
                pendingIntent
            );
        }
        
        // フォアグラウンドサービスとして開始
        startForeground(NOTIFICATION_ID, builder.build());
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}
