package com.yxh.fangs.locaiton;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

public class LocationRepository {

    public interface Callback {
        void onLocationChanged(Location location);

        void onError(String msg);
    }

    private final Context appContext;
    private final LocationManager locationManager;
    private LocationListener locationListener;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private Location lastLocation;

    public LocationRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.locationManager =
                (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * 开始定位（Network 先，GPS 延迟补精度）
     */
    @SuppressLint("MissingPermission")
    public void start(Callback callback) {
        if (!hasPermission()) {
            callback.onError("未授予定位权限");
            return;
        }

        boolean netEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsEnabled =
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!netEnabled && !gpsEnabled) {
            callback.onError("未开启定位服务");
            return;
        }

        locationListener = location -> {
            // ===== 1️⃣ 防抖：位置几乎没变则忽略 =====
            if (lastLocation != null) {
                float distance = location.distanceTo(lastLocation);
                if (distance < 5) { // 5 米以内忽略
                    return;
                }
            }

            lastLocation = location;
            callback.onLocationChanged(location);
        };

        // ===== 2️⃣ 先发 lastKnownLocation（秒出位置）=====
        emitLastKnownLocation(netEnabled, gpsEnabled, callback);

        // ===== 3️⃣ 启动 Network 定位（快速）=====
        if (netEnabled) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    30000,
                    0,
                    locationListener
            );
        }

        // ===== 4️⃣ 延迟启动 GPS（高精度补偿）=====
        if (gpsEnabled) {
            handler.postDelayed(() -> {
                try {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            30000,
                            0,
                            locationListener
                    );
                } catch (Exception ignored) {
                }
            }, 5000); // ⏱ 8 秒后启动 GPS
        }
    }

    /**
     * 立即发出最近一次定位
     */
    @SuppressLint("MissingPermission")
    private void emitLastKnownLocation(
            boolean netEnabled,
            boolean gpsEnabled,
            Callback callback
    ) {
        Location last = null;

        if (netEnabled) {
            last = locationManager.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER
            );
        }

        if (last == null && gpsEnabled) {
            last = locationManager.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER
            );
        }

        if (last != null) {
            lastLocation = last;
            callback.onLocationChanged(last);
        }
    }

    /**
     * 停止定位（必须在 onDestroy 调用）
     */
    public void stop() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
        handler.removeCallbacksAndMessages(null);
    }

    private boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }
}
