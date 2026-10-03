package site.m0d.messenger;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

public class CallForegroundService extends Service {
    public static final String ACTION_START = "site.m0d.messenger.call.START";
    public static final String EXTRA_VIDEO = "video";
    private static final String CHANNEL_ID = "active_calls";
    private static final int NOTIFICATION_ID = 2026;

    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean video = intent != null && intent.getBooleanExtra("video", false);
        acquireLocks();
        startAsForeground(video);
        return START_NOT_STICKY;
    }

    private void startAsForeground(boolean video) {
        Notification notification = buildNotification(video);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int type = ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;
            if (video) type |= ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA;
            startForeground(NOTIFICATION_ID, notification, type);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private Notification buildNotification(boolean video) {
        Intent launch = new Intent(this, MainActivity.class);
        launch.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, launch, flags);

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? new Notification.Builder(this, CHANNEL_ID)
            : new Notification.Builder(this);

        return builder
            .setSmallIcon(R.drawable.ic_stat_m0d)
            .setContentTitle("M0D")
            .setContentText(video ? "Video call in progress" : "Call in progress")
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_CALL)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .setOnlyAlertOnce(true)
            .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID,
            "Active calls",
            NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Keeps an active M0D call running in the background");
        channel.setSound(null, null);
        channel.enableVibration(false);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
    }

    private void acquireLocks() {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "M0D:CallWakeLock");
            wakeLock.setReferenceCounted(false);
        }
        if (!wakeLock.isHeld()) wakeLock.acquire();

        if (wifiLock == null) {
            WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiLock = wifi.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "M0D:CallWifiLock");
            wifiLock.setReferenceCounted(false);
        }
        if (!wifiLock.isHeld()) wifiLock.acquire();
    }

    private void releaseLocks() {
        if (wifiLock != null && wifiLock.isHeld()) wifiLock.release();
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        stopForeground(true);
    }

    @Override
    public void onDestroy() {
        releaseLocks();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
