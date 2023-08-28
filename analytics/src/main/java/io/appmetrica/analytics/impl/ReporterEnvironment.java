package io.appmetrica.analytics.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoWrapper;
import io.appmetrica.analytics.impl.startup.StartupIdentifiersProvider;
import io.appmetrica.analytics.impl.utils.limitation.SimpleMapLimitation;
import io.appmetrica.analytics.internal.CounterConfiguration;

public class ReporterEnvironment extends ClientConfiguration {

    protected ErrorEnvironment mErrorEnvironment;

    protected PreloadInfoWrapper mPreloadInfoWrapper;
    private boolean misSessionPaused = true;
    @Nullable
    private final String initialUserProfileID;

    protected ReporterEnvironment(@NonNull ProcessConfiguration processConfiguration,
                                  @NonNull CounterConfiguration counterConfiguration) {
        this(processConfiguration, counterConfiguration, null);
    }

    protected ReporterEnvironment(@NonNull ProcessConfiguration processConfiguration,
                                  @NonNull CounterConfiguration counterConfiguration,
                                  @Nullable String initialUserProfileID) {
        super(processConfiguration, counterConfiguration);
        this.initialUserProfileID = initialUserProfileID;
    }

    void initialize(final SimpleMapLimitation crashEnvironmentLimitation) {
        mErrorEnvironment = new ErrorEnvironment(crashEnvironmentLimitation);
    }

    public Bundle getConfigBundle() {
        Bundle result = new Bundle();
        getReporterConfiguration().toBundle(result);
        getProcessConfiguration().toBundle(result);
        return result;
    }

    void updateStartupParams(final StartupIdentifiersProvider startupIdentifiersProvider) {
        setConfigIdentifiers(startupIdentifiersProvider);
    }

    void onPauseForegroundSession() {
        misSessionPaused = true;
    }

    void onResumeForegroundSession() {
        misSessionPaused = false;
    }

    boolean isForegroundSessionPaused() {
        return misSessionPaused;
    }

    void setConfigIdentifiers(final StartupIdentifiersProvider startupParamsProvider) {
        if (null != startupParamsProvider) {
            getReporterConfiguration().setUuid(startupParamsProvider.getUuid());
        }
    }

    void putErrorEnvironmentValue(String key, String value) {
        mErrorEnvironment.put(key, value);
    }

    @Nullable
    public String getErrorEnvironment() {
        return mErrorEnvironment.toJsonString();
    }

    PreloadInfoWrapper getPreloadInfoWrapper() {
        return mPreloadInfoWrapper;
    }

    void setPreloadInfoWrapper(PreloadInfoWrapper preloadInfoWrapper) {
        mPreloadInfoWrapper = preloadInfoWrapper;
    }

    @VisibleForTesting
    void setErrorEnvironment(ErrorEnvironment errorEnvironment) {
        mErrorEnvironment = errorEnvironment;
    }

    @Nullable
    public String getInitialUserProfileID() {
        return initialUserProfileID;
    }

}
