package io.appmetrica.analytics.impl;

import android.content.Context;
import android.location.LocationManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable;
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils;
import java.util.Collections;
import java.util.List;

public class AvailableProvidersRetriever {

    @Nullable
    private final LocationManager mLocationManager;

    public AvailableProvidersRetriever(@NonNull Context context) {
        this((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
    }

    @VisibleForTesting
    AvailableProvidersRetriever(@Nullable LocationManager locationManager) {
        mLocationManager = locationManager;
    }

    @NonNull
    public List<String> getAvailableProviders() {
        return SystemServiceUtils.accessSystemServiceSafelyOrDefault(
            mLocationManager,
            "getting available providers",
            "location manager",
            Collections.<String>emptyList(),
            new FunctionWithThrowable<LocationManager, List<String>>() {
                @Override
                public List<String> apply(@NonNull LocationManager input) throws Throwable {
                    return input.getProviders(true);
                }

            }
        );
    }
}
