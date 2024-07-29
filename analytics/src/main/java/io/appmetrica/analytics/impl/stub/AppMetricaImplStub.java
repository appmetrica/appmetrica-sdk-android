package io.appmetrica.analytics.impl.stub;

import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AdvIdentifiersResult;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.StartupParamsCallback;
import io.appmetrica.analytics.impl.FeaturesResult;
import io.appmetrica.analytics.impl.IAppMetricaImpl;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.IReporterFactory;
import io.appmetrica.analytics.impl.MainReporterApiConsumerProvider;
import java.util.List;
import java.util.Map;

public class AppMetricaImplStub implements IAppMetricaImpl {

    private static final String DEVICE_USER_IN_LOCKED_STATE = "Device user is in locked state";

    @Override
    public void activate(@NonNull AppMetricaConfig config) {
        // Do nothing
    }

    @Override
    public void activateAnonymously() {
        // Do nothing
    }

    @Nullable
    @Override
    public MainReporterApiConsumerProvider getMainReporterApiConsumerProvider() {
        return new MainReporterApiConsumerProvider(new MainReporterStub());
    }

    @Override
    public void requestDeferredDeeplinkParameters(DeferredDeeplinkParametersListener listener) {
        // Do nothing
    }

    @Override
    public void requestDeferredDeeplink(DeferredDeeplinkListener listener) {
        // Do nothing
    }

    @Override
    public void activateReporter(@NonNull ReporterConfig config) {
        // Do nothing
    }

    @NonNull
    @Override
    public IReporterExtended getReporter(@NonNull ReporterConfig config) {
        return new ReporterExtendedStub();
    }

    @Nullable
    @Override
    public String getDeviceId() {
        return null;
    }

    @NonNull
    @Override
    public AdvIdentifiersResult getCachedAdvIdentifiers() {
        return new AdvIdentifiersResult(
            new AdvIdentifiersResult.AdvId(
                null,
                AdvIdentifiersResult.Details.INTERNAL_ERROR,
                DEVICE_USER_IN_LOCKED_STATE
            ),
            new AdvIdentifiersResult.AdvId(
                null,
                AdvIdentifiersResult.Details.INTERNAL_ERROR,
                DEVICE_USER_IN_LOCKED_STATE
            ),
            new AdvIdentifiersResult.AdvId(
                null,
                AdvIdentifiersResult.Details.INTERNAL_ERROR,
                DEVICE_USER_IN_LOCKED_STATE
            )
        );
    }

    @Nullable
    @Override
    public Map<String, String> getClids() {
        return null;
    }

    @Override
    public void requestStartupParams(
        @NonNull final StartupParamsCallback callback,
        @NonNull final List<String> params
    ) {
        callback.onRequestError(StartupParamsCallback.Reason.UNKNOWN, null);
    }

    @Override
    public void onReceiveResult(int resultCode, @NonNull Bundle resultData) {
        // Do nothing
    }

    @Override
    public void setLocation(@Nullable Location location) {
        // Do nothing
    }

    @Override
    public void setLocationTracking(boolean enabled) {
        // Do nothing
    }

    @Override
    public void setDataSendingEnabled(boolean value) {
        // Do nothing
    }

    @Override
    public void putAppEnvironmentValue(String key, String value) {
        // Do nothing
    }

    @Override
    public void clearAppEnvironment() {
        // Do nothing
    }

    @Override
    public void putErrorEnvironmentValue(String key, String value) {
        // Do nothing
    }

    @Override
    public void setUserProfileID(@Nullable String userProfileID) {
        // Do nothing
    }

    @NonNull
    @Override
    public IReporterFactory getReporterFactory() {
        return new ReporterFactoryStub();
    }

    @NonNull
    @Override
    public FeaturesResult getFeatures() {
        return new FeaturesResult(null);
    }
}
