package io.appmetrica.analytics.impl;

import android.location.Location;
import androidx.annotation.Nullable;

public interface MetricaConfigurator {

    void setLocation(@Nullable Location location);

    void setLocationTracking(boolean enabled);

    void setDataSendingEnabled(boolean value);

    void putAppEnvironmentValue(String key, String value);

    void clearAppEnvironment();

    void putErrorEnvironmentValue(String key, String value);

    void setUserProfileID(@Nullable String userProfileID);
}
