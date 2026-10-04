package site.m0d.messenger;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class BackgroundNotificationService extends Service {
    public static final String ACTION_START = "site.m0d.messenger.notifications.START";
    public static final String ACTION_STOP = "site.m0d.messenger.notifications.STOP";
    public static final String EXTRA_COOKIE = "cookie";
    public static final String EXTRA_BASE_URL = "base_url";

    private static final String PREFS = "m0d_background_notifications";
    private static final String PREF_COOKIE = "cookie";
    private static final String PREF_BASE_URL = "base_url";
    private static final String STATUS_CHANNEL = "m0d_background_connection";
    private static final String MESSAGE_CHANNEL = "messages";
    private static final String CALL_CHANNEL = "incoming_calls";
    private static final int STATUS_NOTIFICATION_ID = 2041;

    private static volatile boolean appVisible = false;
    private static volatile boolean serviceRunning = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private OkHttpClient client;
    private WebSocket socket;
    private boolean stopping = false;
    private long reconnectDelayMs = 3000;
    private String currentUserId;

    public static void setAppVisible(boolean visible) {
        appVisible = visible;
    }

    public static boolean isRunning() {
        return serviceRunning;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createChannels();
        client = new OkHttpClient.Builder()
            .pingInterval(25, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopping = true;
            clearStoredSession();
            stopSelf();
            return START_NOT_STICKY;
        }

        if (intent != null) {
            String cookie = intent.getStringExtra(EXTRA_COOKIE);
            String baseUrl = intent.getStringExtra(EXTRA_BASE_URL);
            if (cookie != null && !cookie.isEmpty() && baseUrl != null && !baseUrl.isEmpty()) {
                getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString(PREF_COOKIE, cookie)
                    .putString(PREF_BASE_URL, baseUrl)
                    .apply();
            }
        }

        serviceRunning = true;
        stopping = false;
        startAsForeground();
        connect();
        return START_STICKY;
    }

    private void startAsForeground() {
        Notification notification = buildStatusNotification();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                STATUS_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
            );
        } else {
            startForeground(STATUS_NOTIFICATION_ID, notification);
        }
    }

    private void connect() {
        if (stopping || socket != null) return;

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String cookie = prefs.getString(PREF_COOKIE, "");
        String baseUrl = prefs.getString(PREF_BASE_URL, "");
        if (cookie.isEmpty() || baseUrl.isEmpty()) {
            stopSelf();
            return;
        }

        String wsUrl;
        if (baseUrl.startsWith("https://")) wsUrl = "wss://" + baseUrl.substring(8) + "/ws";
        else if (baseUrl.startsWith("http://")) wsUrl = "ws://" + baseUrl.substring(7) + "/ws";
        else {
            stopSelf();
            return;
        }

        Request request = new Request.Builder()
            .url(wsUrl)
            .header("Cookie", cookie)
            .header("User-Agent", "M0D-Android-Background")
            .build();

        socket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                reconnectDelayMs = 3000;
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                handleSocketMessage(text);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                socket = null;
                scheduleReconnect();
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                socket = null;
                if (response != null && response.code() == 401) {
                    stopping = true;
                    clearStoredSession();
                    stopSelf();
                    return;
                }
                scheduleReconnect();
            }
        });
    }

    private void handleSocketMessage(String text) {
        try {
            JSONObject event = new JSONObject(text);
            String type = event.optString("type", "");
            if ("ready".equals(type)) {
                currentUserId = event.optString("userId", null);
                return;
            }

            if ("message".equals(type)) {
                JSONObject message = event.optJSONObject("message");
                if (message == null) return;
                String senderId = message.optString("sender_id", "");
                if (currentUserId != null && currentUserId.equals(senderId)) return;
                if (appVisible) return;
                String conversationId = message.optString("conversation_id", "");
                long id = message.optLong("id", System.currentTimeMillis());
                showMessageNotification(conversationId, id);
                return;
            }

            if ("call-request".equals(type) && !appVisible) {
                String conversationId = event.optString("conversationId", "");
                boolean video = event.optBoolean("video", false);
                showCallNotification(conversationId, video);
            }
        } catch (Exception ignored) {
        }
    }

    private void scheduleReconnect() {
        if (stopping) return;
        long delay = reconnectDelayMs;
        reconnectDelayMs = Math.min(reconnectDelayMs * 2, 30000);
        handler.postDelayed(this::connect, delay);
    }

    private Notification buildStatusNotification() {
        PendingIntent content = launchAppIntent(100, null);
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? new Notification.Builder(this, STATUS_CHANNEL)
            : new Notification.Builder(this);
        return builder
            .setSmallIcon(R.drawable.ic_stat_m0d)
            .setContentTitle("M0D")
            .setContentText("Уведомления работают в фоне")
            .setContentIntent(content)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .build();
    }

    private void showMessageNotification(String conversationId, long seed) {
        PendingIntent content = launchAppIntent((int) (seed % 100000) + 1000, conversationId);
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? new Notification.Builder(this, MESSAGE_CHANNEL)
            : new Notification.Builder(this);
        Notification notification = builder
            .setSmallIcon(R.drawable.ic_stat_m0d)
            .setContentTitle("M0D")
            .setContentText("Новое сообщение")
            .setContentIntent(content)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .build();
        getSystemService(NotificationManager.class).notify(
            Math.max(1, (int) (Math.abs(seed) % 2000000000L)),
            notification
        );
    }

    private void showCallNotification(String conversationId, boolean video) {
        PendingIntent content = launchAppIntent(3030, conversationId);
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? new Notification.Builder(this, CALL_CHANNEL)
            : new Notification.Builder(this);
        Notification notification = builder
            .setSmallIcon(R.drawable.ic_stat_m0d)
            .setContentTitle("M0D")
            .setContentText(video ? "Входящий видеозвонок" : "Входящий звонок")
            .setContentIntent(content)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_CALL)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .build();
        getSystemService(NotificationManager.class).notify(3030, notification);
    }

    private PendingIntent launchAppIntent(int requestCode, String conversationId) {
        Intent launch = new Intent(this, MainActivity.class);
        launch.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (conversationId != null && !conversationId.isEmpty()) {
            launch.putExtra("m0d_conversation_id", conversationId);
        }
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getActivity(this, requestCode, launch, flags);
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel status = new NotificationChannel(
            STATUS_CHANNEL,
            "Фоновая работа M0D",
            NotificationManager.IMPORTANCE_LOW
        );
        status.setDescription("Поддерживает доставку уведомлений M0D в фоне");
        status.setSound(null, null);
        status.enableVibration(false);
        status.setShowBadge(false);
        manager.createNotificationChannel(status);

        NotificationChannel messages = new NotificationChannel(
            MESSAGE_CHANNEL,
            "Сообщения",
            NotificationManager.IMPORTANCE_HIGH
        );
        messages.setDescription("Новые сообщения M0D");
        messages.enableVibration(true);
        manager.createNotificationChannel(messages);

        NotificationChannel calls = new NotificationChannel(
            CALL_CHANNEL,
            "Входящие звонки",
            NotificationManager.IMPORTANCE_HIGH
        );
        calls.setDescription("Входящие звонки M0D");
        calls.enableVibration(true);
        manager.createNotificationChannel(calls);
    }

    private void clearStoredSession() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().clear().apply();
    }

    @Override
    public void onDestroy() {
        stopping = true;
        serviceRunning = false;
        handler.removeCallbacksAndMessages(null);
        if (socket != null) {
            socket.close(1000, "service_stop");
            socket = null;
        }
        if (client != null) {
            client.dispatcher().cancelAll();
            client.dispatcher().executorService().shutdown();
        }
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
