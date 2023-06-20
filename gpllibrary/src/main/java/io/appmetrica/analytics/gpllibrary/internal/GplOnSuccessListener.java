package io.appmetrica.analytics.gpllibrary.internal;

import android.location.Location;
import android.location.LocationListener;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnSuccessListener;

class GplOnSuccessListener implements OnSuccessListener<Location> {

    @NonNull
    private final LocationListener mLocationListener;

    GplOnSuccessListener(@NonNull LocationListener locationListener) {
        mLocationListener = locationListener;
    }

    @Override
    public void onSuccess(Location location) {
        mLocationListener.onLocationChanged(location);
    }
}
