package io.appmetrica.analytics.gpllibrary.internal;

import android.location.LocationListener;
import androidx.annotation.NonNull;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

class GplLocationCallback extends LocationCallback {

    @NonNull
    private final LocationListener mLocationListener;

    GplLocationCallback(@NonNull LocationListener locationListener) {
        mLocationListener = locationListener;
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        mLocationListener.onLocationChanged(locationResult.getLastLocation());
    }
}
