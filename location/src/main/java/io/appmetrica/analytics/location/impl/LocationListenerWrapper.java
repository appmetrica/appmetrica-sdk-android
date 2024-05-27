package io.appmetrica.analytics.location.impl;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.annotations.GeoThread;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class LocationListenerWrapper implements LocationListener {

    private static final String TAG = "[LocationListenerWrapper]";

    @NonNull
    private final LocationStreamDispatcher mLocationStreamDispatcher;

    public LocationListenerWrapper(@NonNull LocationStreamDispatcher locationStreamDispatcher) {
        mLocationStreamDispatcher = locationStreamDispatcher;
    }

    @GeoThread
    @Override
    public void onLocationChanged(@Nullable Location location) {
        DebugLogger.INSTANCE.info(TAG, "Location changed: %s", location);
        if (location != null) {
            mLocationStreamDispatcher.onLocationChanged(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // do nothing
    }

    @Override
    public void onProviderEnabled(String provider) {
        // do nothing
    }

    @Override
    public void onProviderDisabled(String provider) {
        // do nothing
    }
}
