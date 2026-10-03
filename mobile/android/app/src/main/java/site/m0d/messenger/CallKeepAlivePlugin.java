package site.m0d.messenger;

import android.content.Intent;
import android.os.Build;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "CallKeepAlive")
public class CallKeepAlivePlugin extends Plugin {
    private void startService(boolean video) {
        Intent intent = new Intent(getContext(), CallForegroundService.class);
        intent.setAction(CallForegroundService.ACTION_START);
        intent.putExtra(CallForegroundService.EXTRA_VIDEO, video);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().startForegroundService(intent);
        } else {
            getContext().startService(intent);
        }
    }

    @PluginMethod
    public void start(PluginCall call) {
        boolean video = call.getBoolean("video", false);
        startService(video);
        JSObject result = new JSObject();
        result.put("active", true);
        result.put("video", video);
        call.resolve(result);
    }

    @PluginMethod
    public void update(PluginCall call) {
        boolean video = call.getBoolean("video", false);
        startService(video);
        JSObject result = new JSObject();
        result.put("active", true);
        result.put("video", video);
        call.resolve(result);
    }

    @PluginMethod
    public void stop(PluginCall call) {
        getContext().stopService(new Intent(getContext(), CallForegroundService.class));
        call.resolve();
    }
}
