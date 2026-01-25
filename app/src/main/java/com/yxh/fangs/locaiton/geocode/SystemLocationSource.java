package com.yxh.fangs.locaiton.geocode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.yxh.fangs.locaiton.model.LocationPoint;

import io.reactivex.rxjava3.core.Observable;

public class SystemLocationSource implements LocationDataSource {

    private final Context context;
    private final LocationManager locationManager;

    public SystemLocationSource(Context context) {
        this.context = context.getApplicationContext();
        this.locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public Observable<LocationPoint> getCurrentLocation() {
        return Observable.create(emitter -> {
            if (!isAvailable()) {
                emitter.onError(new Exception("Location service disabled"));
                return;
            }
            @SuppressLint("MissingPermission")
            Location location =
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                emitter.onNext(new LocationPoint(
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getTime()
                ));
                emitter.onComplete();
            } else {
                emitter.onError(new Exception("Location is null"));
            }
        });
    }

    @Override
    public boolean isAvailable() {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
