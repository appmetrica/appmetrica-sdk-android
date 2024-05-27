package io.appmetrica.analytics.location.impl;

import android.content.Context;
import android.location.Location;
import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.annotations.GeoThread;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor;
import io.appmetrica.analytics.coreutils.internal.cache.LocationDataCacheUpdateScheduler;
import io.appmetrica.analytics.locationapi.internal.ILastKnownUpdater;
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractor;
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProvider;
import io.appmetrica.analytics.locationapi.internal.LocationControllerObserver;
import io.appmetrica.analytics.locationapi.internal.LocationReceiver;
import io.appmetrica.analytics.locationapi.internal.LocationReceiverProvider;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.HashMap;
import java.util.Map;

public class LocationCore implements ILastKnownUpdater, LocationControllerObserver {

    private static final String TAG = "[LocationCore]";

    @NonNull
    private final Context context;
    @NonNull
    private final PermissionExtractor permissionExtractor;
    @NonNull
    private final LocationStreamDispatcher locationStreamDispatcher;
    @NonNull
    private final IHandlerExecutor executor;
    @NonNull
    private final LocationListenerWrapper locationListener;
    @NonNull
    private final LocationDataCacheUpdateScheduler cacheUpdateScheduler;
    private boolean locationTrackingStarted;
    @NonNull
    private final Map<String, LastKnownLocationExtractor> lastKnownLocationExtractors = new HashMap<>();
    @NonNull
    private final Map<String, LocationReceiver> locationReceivers = new HashMap<>();

    public LocationCore(@NonNull Context context,
                        @NonNull PermissionExtractor permissionExtractor,
                        @NonNull IHandlerExecutor executor,
                        @NonNull LocationStreamDispatcher locationStreamDispatcher) {
        this.context = context;
        this.locationStreamDispatcher = locationStreamDispatcher;
        this.permissionExtractor = permissionExtractor;
        this.executor = executor;
        locationListener = new LocationListenerWrapper(locationStreamDispatcher);
        cacheUpdateScheduler = new LocationDataCacheUpdateScheduler(
            executor,
            this,
            this.locationStreamDispatcher.getLocationDataCache(),
            "loc"
        );
        this.locationStreamDispatcher.getLocationDataCache().setUpdateScheduler(cacheUpdateScheduler);
    }

    @Override
    @GeoThread
    public synchronized void updateLastKnown() {
        for (LastKnownLocationExtractor extractor : lastKnownLocationExtractors.values()) {
            extractor.updateLastKnownLocation();
        }
    }

    @Nullable
    public Location getCachedLocation() {
        Location location = locationStreamDispatcher.getCachedLocation();
        DebugLogger.INSTANCE.info(TAG, "getCachedLocation: %s", location);
        return location;
    }

    @Override
    @GeoThread
    public synchronized void startLocationTracking() {
        if (!locationTrackingStarted) {
            locationTrackingStarted = true;
            DebugLogger.INSTANCE.info(TAG, "Start location tracking");
            startLocationTrackingInternal();
        } else {
            DebugLogger.INSTANCE.info(TAG, "Location tracking has already been started");
        }
    }

    @GeoThread
    private synchronized void startLocationTrackingInternal() {
        DebugLogger.INSTANCE.info(TAG, "Start location tracking internal");
        cacheUpdateScheduler.startUpdates();
        for (LocationReceiver locationReceiver : locationReceivers.values()) {
            locationReceiver.startLocationUpdates();
        }
        updateLastKnown();
    }

    @Override
    @GeoThread
    public synchronized void stopLocationTracking() {
        if (locationTrackingStarted) {
            DebugLogger.INSTANCE.info(TAG, "Stop location tracking");
            locationTrackingStarted = false;
            stopLocationTrackingInternal();
        } else {
            DebugLogger.INSTANCE.info(
                TAG,
                "Location tracking hasn't been started. So ingnore stopLocationTracking."
            );
        }
    }

    @GeoThread
    private synchronized void stopLocationTrackingInternal() {
        DebugLogger.INSTANCE.info(TAG, "Stop location tracking internal");
        cacheUpdateScheduler.stopUpdates();
        for (LocationReceiver locationReceiver : locationReceivers.values()) {
            locationReceiver.stopLocationUpdates();
        }
    }

    @AnyThread
    public void updateConfig(@NonNull final LocationConfig locationConfig) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                locationStreamDispatcher.setLocationConfig(locationConfig);
            }
        });
    }

    @AnyThread
    public synchronized void registerLastKnownSource(@NonNull LastKnownLocationExtractorProvider sourceProvider) {
        LastKnownLocationExtractor source =
            sourceProvider.getExtractor(context, permissionExtractor, executor, locationListener);
        DebugLogger.INSTANCE.info(
            TAG,
            "registerLastKnownLocationExtractor: %s with id = %s",
            source,
            sourceProvider.getIdentifier()
        );
        lastKnownLocationExtractors.put(sourceProvider.getIdentifier(), source);
        if (locationTrackingStarted) {
            source.updateLastKnownLocation();
        }
    }

    @AnyThread
    public synchronized void unregisterLastKnownSource(@NonNull LastKnownLocationExtractorProvider sourceProvider) {
        DebugLogger.INSTANCE.info(
            TAG,
            "unregisterLastKnownLocationExtractor: %s",
            sourceProvider.getIdentifier()
        );
        lastKnownLocationExtractors.remove(sourceProvider.getIdentifier());
    }

    @AnyThread
    public synchronized void registerLocationReceiver(@NonNull LocationReceiverProvider receiverProvider) {
        LocationReceiver receiver =
            receiverProvider.getLocationReceiver(context, permissionExtractor, executor, locationListener);
        DebugLogger.INSTANCE.info(
            TAG,
            "register location receiver: %s with id = %s; locationTrackingStarted = %s",
            receiver,
            receiverProvider.getIdentifier(),
            locationTrackingStarted
        );
        LocationReceiver oldReceiver = locationReceivers.put(receiverProvider.getIdentifier(), receiver);
        if (locationTrackingStarted) {
            if (oldReceiver != null) {
                DebugLogger.INSTANCE.info(TAG, "stop prev location receiver: %s with id = %s",
                    oldReceiver, receiverProvider.getIdentifier());
                oldReceiver.stopLocationUpdates();
            }
            DebugLogger.INSTANCE.info(TAG, "start location updates for receiver = %s", receiver);
            receiver.startLocationUpdates();
        }
    }

    @AnyThread
    public synchronized void unregisterLocationReceiver(@NonNull LocationReceiverProvider receiverProvider) {
        DebugLogger.INSTANCE.info(
            TAG,
            "unregister location receiver with id: %s; locationTrackingStarted = %s",
            receiverProvider.getIdentifier(),
            locationTrackingStarted
        );
        LocationReceiver receiver = locationReceivers.remove(receiverProvider.getIdentifier());
        if (receiver != null) {
            if (locationTrackingStarted) {
                DebugLogger.INSTANCE.info(
                    TAG,
                    "stop location updates for receiver with id = %s",
                    receiverProvider.getIdentifier()
                );
                receiver.stopLocationUpdates();
            }
        }
    }
}
