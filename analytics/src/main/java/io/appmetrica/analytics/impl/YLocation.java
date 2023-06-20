package io.appmetrica.analytics.impl;

import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.StringUtils;

public final class YLocation extends Location {

    @Nullable
    private final String mOriginalProvider;

    private YLocation(@NonNull Location originalLocation, @Nullable String originalProvider) {
        super(originalLocation);
        mOriginalProvider = originalProvider;
    }

    @Nullable
    public String getOriginalProvider() {
        return mOriginalProvider;
    }

    @NonNull
    public static YLocation createWithOriginalProvider(@NonNull Location originalLocation) {
        Location locationCopy = new Location(originalLocation);
        final String originalProvider = locationCopy.getProvider();
        locationCopy.setProvider(StringUtils.EMPTY);
        return new YLocation(locationCopy, originalProvider);
    }

    @NonNull
    public static YLocation createWithoutOriginalProvider(@NonNull Location originalLocation) {
        return new YLocation(new Location(originalLocation), StringUtils.EMPTY);
    }
}
