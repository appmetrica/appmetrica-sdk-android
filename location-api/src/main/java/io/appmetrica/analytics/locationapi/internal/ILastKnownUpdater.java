package io.appmetrica.analytics.locationapi.internal;

import io.appmetrica.analytics.coreapi.internal.annotations.GeoThread;

public interface ILastKnownUpdater {

    @GeoThread
    void updateLastKnown();
}
