package io.appmetrica.analytics.impl.stub;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AdRevenue;
import io.appmetrica.analytics.ModuleEvent;
import io.appmetrica.analytics.Revenue;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.crash.jvm.client.AllThreads;
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException;
import io.appmetrica.analytics.plugins.IPluginReporter;
import io.appmetrica.analytics.profile.UserProfile;
import java.util.Map;

public class ReporterExtendedStub implements IReporterExtended {

    private final PluginReporterStub pluginReporterStub = new PluginReporterStub();

    @Override
    public void putAppEnvironmentValue(@NonNull String key,
                                       @Nullable String value) {
        //Do nothing
    }

    @Override
    public void clearAppEnvironment() {
        //Do nothing
    }

    @Override
    public void sendEventsBuffer() {
        //Do nothing
    }

    @Override
    public void reportEvent(@NonNull String eventName) {
        //Do nothing
    }

    @Override
    public void reportEvent(@NonNull String eventName,
                            @Nullable String jsonValue) {
        //Do nothing
    }

    @Override
    public void reportEvent(@NonNull String eventName,
                            @Nullable Map<String, Object> attributes) {
        //Do nothing
    }

    @Override
    public void reportError(@NonNull String message,
                            @Nullable Throwable error) {
        //Do nothing
    }

    @Override
    public void reportError(@NonNull String identifier,
                            @Nullable String message) {
        //Do nothing
    }

    @Override
    public void reportError(@NonNull String identifier,
                            @Nullable String message,
                            @Nullable Throwable error) {
        //Do nothing
    }

    @Override
    public void reportUnhandledException(@NonNull Throwable exception) {
        //Do nothing
    }

    @Override
    public void resumeSession() {
        //Do nothing
    }

    @Override
    public void pauseSession() {
        //Do nothing
    }

    @Override
    public void setUserProfileID(@Nullable String profileID) {
        //Do nothing
    }

    @Override
    public void reportUserProfile(@NonNull UserProfile profile) {
        //Do nothing
    }

    @Override
    public void reportRevenue(@NonNull Revenue revenue) {
        //Do nothing
    }

    @Override
    public void reportECommerce(@NonNull ECommerceEvent event) {
        //Do nothing
    }

    @Override
    public void setDataSendingEnabled(boolean enabled) {
        //Do nothing
    }

    @NonNull
    @Override
    public IPluginReporter getPluginExtension() {
        return pluginReporterStub;
    }

    @Override
    public void reportAdRevenue(@NonNull AdRevenue adRevenue) {
        //Do nothing
    }

    @Override
    public void reportUnhandledException(@NonNull UnhandledException unhandledException) {
        //Do nothing
    }

    @Override
    public void reportAnr(@NonNull AllThreads allThreads) {
        //Do nothing
    }

    @Override
    public void reportEvent(
        @NonNull final ModuleEvent moduleEvent
    ) {
        //Do nothing
    }

    @Override
    public void setSessionExtra(@NonNull String key, @Nullable byte[] value) {
        //Do nothing
    }

    @Override
    public void reportAdRevenue(@NonNull AdRevenue adRevenue, boolean autoCollected) {
        //Do nothing
    }
}
