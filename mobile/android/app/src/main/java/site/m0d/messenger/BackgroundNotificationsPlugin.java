package site.m0d.messenger;

import android.content.Intent;
import android.os.Build;
import android.webkit.CookieManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "BackgroundNotifications")
public class BackgroundNotificationsPlugin extends Plugin {
    @PluginMethod
    public void start(PluginCall call) {
        String baseUrl = call.getString("baseUrl", "https://m0d-dev.mask-0f-darkness.ru");
        if (baseUrl == null || !baseUrl.startsWith("https://")) {
            call.reject("invalid_base_url");
            return;
        }

        String cookies = CookieManager.getInstance().getCookie(baseUrl);
        String sessionCookie = extractSessionCookie(cookies);
        if (sessionCookie == null) {
            JSObject result = new JSObject();
            result.put("active", false);
            result.put("reason", "no_session");
            call.resolve(result);
            return;
        }

        Intent intent = new Intent(getContext(), BackgroundNotificationService.class);
        intent.setAction(BackgroundNotificationService.ACTION_START);
        intent.putExtra(BackgroundNotificationService.EXTRA_COOKIE, sessionCookie);
        intent.putExtra(BackgroundNotificationService.EXTRA_BASE_URL, baseUrl);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().startForegroundService(intent);
        } else {
            getContext().startService(intent);
        }

        JSObject result = new JSObject();
        result.put("active", true);
        call.resolve(result);
    }

    @PluginMethod
    public void stop(PluginCall call) {
        Intent intent = new Intent(getContext(), BackgroundNotificationService.class);
        intent.setAction(BackgroundNotificationService.ACTION_STOP);
        getContext().startService(intent);
        JSObject result = new JSObject();
        result.put("active", false);
        call.resolve(result);
    }

    @PluginMethod
    public void status(PluginCall call) {
        JSObject result = new JSObject();
        result.put("active", BackgroundNotificationService.isRunning());
        call.resolve(result);
    }

    private String extractSessionCookie(String cookies) {
        if (cookies == null || cookies.isEmpty()) return null;
        for (String part : cookies.split(";")) {
            String trimmed = part.trim();
            if (trimmed.startsWith("m0d_session=") && trimmed.length() > "m0d_session=".length()) {
                return trimmed;
            }
        }
        return null;
    }
}
