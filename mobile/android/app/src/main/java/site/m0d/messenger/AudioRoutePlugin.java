package site.m0d.messenger;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "AudioRoute")
public class AudioRoutePlugin extends Plugin {
    private AudioManager audioManager() {
        return (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    @PluginMethod
    public void setSpeakerphone(PluginCall call) {
        boolean enabled = call.getBoolean("enabled", true);
        AudioManager audio = audioManager();
        audio.setMode(AudioManager.MODE_IN_COMMUNICATION);
        boolean applied = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            int wanted = enabled ? AudioDeviceInfo.TYPE_BUILTIN_SPEAKER : AudioDeviceInfo.TYPE_BUILTIN_EARPIECE;
            for (AudioDeviceInfo device : audio.getAvailableCommunicationDevices()) {
                if (device.getType() == wanted) {
                    applied = audio.setCommunicationDevice(device);
                    if (applied) break;
                }
            }
        } else {
            audio.setSpeakerphoneOn(enabled);
            applied = true;
        }

        JSObject result = new JSObject();
        result.put("enabled", enabled);
        result.put("applied", applied);
        call.resolve(result);
    }

    @PluginMethod
    public void setMicrophoneMuted(PluginCall call) {
        boolean muted = call.getBoolean("muted", false);
        AudioManager audio = audioManager();
        audio.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audio.setMicrophoneMute(muted);
        JSObject result = new JSObject();
        result.put("muted", audio.isMicrophoneMute());
        call.resolve(result);
    }

    @PluginMethod
    public void reset(PluginCall call) {
        AudioManager audio = audioManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) audio.clearCommunicationDevice();
        else audio.setSpeakerphoneOn(false);
        audio.setMicrophoneMute(false);
        audio.setMode(AudioManager.MODE_NORMAL);
        call.resolve();
    }
}
