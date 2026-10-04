package site.m0d.messenger;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(AudioRoutePlugin.class);
        registerPlugin(CallKeepAlivePlugin.class);
        registerPlugin(AppUpdaterPlugin.class);
        registerPlugin(BackgroundNotificationsPlugin.class);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        BackgroundNotificationService.setAppVisible(true);
    }

    @Override
    protected void onStop() {
        BackgroundNotificationService.setAppVisible(false);
        super.onStop();
    }
}
