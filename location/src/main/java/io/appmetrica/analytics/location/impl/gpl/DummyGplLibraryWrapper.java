package io.appmetrica.analytics.location.impl.gpl;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.gpllibrary.internal.GplLibraryWrapper;
import io.appmetrica.analytics.gpllibrary.internal.IGplLibraryWrapper;

public class DummyGplLibraryWrapper implements IGplLibraryWrapper {

    @Override
    public void updateLastKnownLocation() throws Throwable {
        // do nothing
    }

    @Override
    public void startLocationUpdates(@NonNull GplLibraryWrapper.Priority priority) throws Throwable {
        // do nothing
    }

    @Override
    public void stopLocationUpdates() throws Throwable {
        // do nothing
    }
}
