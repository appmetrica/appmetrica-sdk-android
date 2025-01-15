package io.appmetrica.analytics.impl.proxy;

import android.content.Context;
import android.location.Location;
import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.impl.AppMetricaFacade;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class AppMetricaFacadeProvider {

    private static final String TAG = "[AppMetricaFacadeProvider]";

    @AnyThread
    @NonNull
    public AppMetricaFacade getInitializedImpl(@NonNull Context context) {
        DebugLogger.INSTANCE.info(TAG, "getInitializedImpl");
        return AppMetricaFacade.getInstance(context);
    }

    @AnyThread
    @Nullable
    public AppMetricaFacade peekInitializedImpl() {
        return AppMetricaFacade.peekInstance();
    }

    @AnyThread
    public void markActivated() {
        AppMetricaFacade.markActivated();
    }

    @AnyThread
    public boolean isActivated() {
        return AppMetricaFacade.isActivated();
    }

    @AnyThread
    public boolean isInitializedForApp() {
        return AppMetricaFacade.isInitializedForApp();
    }

    @WorkerThread
    public void setLocation(@Nullable final Location location) {
        AppMetricaFacade.setLocation(location);
    }

    @WorkerThread
    public void setLocationTracking(final boolean enabled) {
        AppMetricaFacade.setLocationTracking(enabled);
    }

    @WorkerThread
    public void setAdvIdentifiersTracking(final boolean enabled) {
        AppMetricaFacade.setAdvIdentifiersTracking(enabled);
    }

    @WorkerThread
    public void setDataSendingEnabled(final boolean enabled) {
        AppMetricaFacade.setDataSendingEnabled(enabled);
    }

    @WorkerThread
    public void putErrorEnvironmentValue(String key, String value) {
        AppMetricaFacade.putErrorEnvironmentValue(key, value);
    }

    @WorkerThread
    public void putAppEnvironmentValue(String key, String value) {
        AppMetricaFacade.putAppEnvironmentValue(key, value);
    }

    @WorkerThread
    public void clearAppEnvironment() {
        AppMetricaFacade.clearAppEnvironment();
    }

    @WorkerThread
    public void setUserProfileID(@Nullable String userProfileID) {
        AppMetricaFacade.setUserProfileID(userProfileID);
    }

}
