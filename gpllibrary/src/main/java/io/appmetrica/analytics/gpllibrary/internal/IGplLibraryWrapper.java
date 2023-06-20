package io.appmetrica.analytics.gpllibrary.internal;

import androidx.annotation.NonNull;

public interface IGplLibraryWrapper {

    void updateLastKnownLocation() throws Throwable;

    void startLocationUpdates(@NonNull GplLibraryWrapper.Priority priority) throws Throwable;

    void stopLocationUpdates() throws Throwable;
}
