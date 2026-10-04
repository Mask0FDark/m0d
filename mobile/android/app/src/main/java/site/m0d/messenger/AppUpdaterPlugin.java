package site.m0d.messenger;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.FileProvider;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@CapacitorPlugin(name = "AppUpdater")
public class AppUpdaterPlugin extends Plugin {
    private static final String RELEASES_API =
            "https://api.github.com/repos/Mask0FDark/m0d/releases?per_page=10";

    private String currentVersion() {
        try {
            return getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException error) {
            return "0.0.0";
        }
    }

    private static int compareVersions(String left, String right) {
        String[] a = String.valueOf(left).replaceAll("[^0-9.].*$", "").split("\\.");
        String[] b = String.valueOf(right).replaceAll("[^0-9.].*$", "").split("\\.");
        int length = Math.max(a.length, b.length);
        for (int i = 0; i < length; i++) {
            int av = i < a.length && !a[i].isEmpty() ? Integer.parseInt(a[i]) : 0;
            int bv = i < b.length && !b[i].isEmpty() ? Integer.parseInt(b[i]) : 0;
            if (av != bv) return Integer.compare(av, bv);
        }
        return 0;
    }

    private static String readText(HttpURLConnection connection) throws Exception {
        try (BufferedInputStream input = new BufferedInputStream(connection.getInputStream());
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[16 * 1024];
            int read;
            while ((read = input.read(buffer)) >= 0) output.write(buffer, 0, read);
            return output.toString(StandardCharsets.UTF_8.name());
        }
    }

    private JSONArray fetchReleases() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(RELEASES_API).openConnection();
        connection.setRequestProperty("User-Agent", "M0D-Android");
        connection.setRequestProperty("Accept", "application/vnd.github+json");
        connection.setConnectTimeout(12000);
        connection.setReadTimeout(12000);
        try {
            if (connection.getResponseCode() != 200) {
                throw new IllegalStateException("release_http_" + connection.getResponseCode());
            }
            return new JSONArray(readText(connection));
        } finally {
            connection.disconnect();
        }
    }

    private String androidAssetUrl(JSONObject release) {
        JSONArray assets = release.optJSONArray("assets");
        if (assets == null) return null;
        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.optJSONObject(i);
            if (asset == null) continue;
            String name = asset.optString("name", "").toLowerCase();
            if (name.matches("m0d-.*-android\\.apk")) {
                return asset.optString("browser_download_url", null);
            }
        }
        return null;
    }

    private JSONObject latestAndroidRelease() throws Exception {
        JSONArray releases = fetchReleases();
        JSONObject best = null;
        String bestVersion = "0.0.0";
        for (int i = 0; i < releases.length(); i++) {
            JSONObject release = releases.optJSONObject(i);
            if (release == null || release.optBoolean("draft", false)) continue;
            String assetUrl = androidAssetUrl(release);
            if (assetUrl == null) continue;
            String version = release.optString("tag_name", "").replaceFirst("^v", "");
            if (best == null || compareVersions(version, bestVersion) > 0) {
                best = release;
                bestVersion = version;
            }
        }
        if (best == null) throw new IllegalStateException("android_release_not_found");
        return best;
    }

    @PluginMethod
    public void getVersion(PluginCall call) {
        JSObject result = new JSObject();
        result.put("version", currentVersion());
        call.resolve(result);
    }

    @PluginMethod
    public void check(PluginCall call) {
        new Thread(() -> {
            try {
                JSONObject release = latestAndroidRelease();
                String latest = release.optString("tag_name", "").replaceFirst("^v", "");
                JSObject result = new JSObject();
                result.put("current", currentVersion());
                result.put("latest", latest);
                result.put("available", compareVersions(currentVersion(), latest) < 0);
                call.resolve(result);
            } catch (Exception error) {
                call.reject("update_check_failed", error);
            }
        }, "m0d-update-check").start();
    }

    @PluginMethod
    public void checkAndInstall(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                !getContext().getPackageManager().canRequestPackageInstalls()) {
            Intent permission = new Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + getContext().getPackageName())
            );
            permission.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(permission);
            JSObject result = new JSObject();
            result.put("status", "permission_required");
            call.resolve(result);
            return;
        }

        new Thread(() -> {
            try {
                JSONObject release = latestAndroidRelease();
                String latest = release.optString("tag_name", "").replaceFirst("^v", "");
                String url = androidAssetUrl(release);
                if (url == null || compareVersions(currentVersion(), latest) >= 0) {
                    JSObject result = new JSObject();
                    result.put("status", "current");
                    result.put("current", currentVersion());
                    result.put("latest", latest);
                    call.resolve(result);
                    return;
                }

                File directory = new File(getContext().getCacheDir(), "updates");
                if (!directory.exists() && !directory.mkdirs()) {
                    throw new IllegalStateException("cannot_create_update_dir");
                }
                File apk = new File(directory, "M0D-" + latest + ".apk");

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestProperty("User-Agent", "M0D-Android");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(60000);
                connection.setInstanceFollowRedirects(true);
                int status = connection.getResponseCode();
                if (status < 200 || status >= 300) {
                    connection.disconnect();
                    throw new IllegalStateException("download_http_" + status);
                }
                try (BufferedInputStream input = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream output = new FileOutputStream(apk)) {
                    byte[] buffer = new byte[64 * 1024];
                    int read;
                    while ((read = input.read(buffer)) >= 0) output.write(buffer, 0, read);
                } finally {
                    connection.disconnect();
                }
                if (!apk.exists() || apk.length() < 1024 * 1024) {
                    throw new IllegalStateException("download_too_small");
                }

                getActivity().runOnUiThread(() -> {
                    try {
                        Uri uri = FileProvider.getUriForFile(
                                getContext(),
                                getContext().getPackageName() + ".fileprovider",
                                apk
                        );
                        Intent install = new Intent(Intent.ACTION_VIEW);
                        install.setDataAndType(uri, "application/vnd.android.package-archive");
                        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        getActivity().startActivity(install);
                        JSObject result = new JSObject();
                        result.put("status", "installer_opened");
                        result.put("latest", latest);
                        call.resolve(result);
                    } catch (Exception error) {
                        call.reject("installer_open_failed", error);
                    }
                });
            } catch (Exception error) {
                call.reject("update_install_failed", error);
            }
        }, "m0d-update-install").start();
    }
}
