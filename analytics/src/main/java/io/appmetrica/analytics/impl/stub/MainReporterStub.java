package io.appmetrica.analytics.impl.stub;

import android.app.Activity;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AdRevenue;
import io.appmetrica.analytics.AnrListener;
import io.appmetrica.analytics.ExternalAttribution;
import io.appmetrica.analytics.ModuleEvent;
import io.appmetrica.analytics.impl.ActivityLifecycleManager;
import io.appmetrica.analytics.impl.IMainReporter;
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler;
import io.appmetrica.analytics.plugins.PluginErrorDetails;
import java.util.ArrayList;
import java.util.List;

public class MainReporterStub extends ReporterExtendedStub implements IMainReporter {

    @Override
    public void enableAnrMonitoring() {
        // Do nothing
    }

    @Override
    public void reportAppOpen(@NonNull String deeplink, boolean auto) {
        //Do nothing
    }

    @Override
    public void reportReferralUrl(@NonNull String referralUrl) {
        //Do nothing
    }

    @Override
    public void onEnableAutoTrackingAttemptOccurred(@NonNull ActivityLifecycleManager.WatchingStatus status) {
        //Do nothing
    }

    @Override
    public void resumeSession(@Nullable Activity activity) {
        //Do nothing
    }

    @Override
    public void pauseSession(@Nullable Activity activity) {
        //Do nothing
    }

    @Override
    public List<String> getCustomHosts() {
        return new ArrayList<String>();
    }

    @Override
    public void reportEvent(
        @NonNull final ModuleEvent moduleEvent
    ) {
        //Do nothing
    }

    @Override
    public void reportAdRevenue(@NonNull AdRevenue adRevenue, boolean autoCollected) {
        //Do nothing
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public void setLocation(@Nullable Location location) {
        //Do nothing
    }

    @Override
    public void setLocationTracking(boolean enabled) {
        //Do nothing
    }

    @Override
    public void setAdvIdentifiersTracking(boolean enabled, boolean force) {
        // Do nothing
    }

    @Override
    public void putErrorEnvironmentValue(String key, String value) {
        //Do nothing
    }

    @Override
    public void reportJsEvent(@NonNull String eventName, @Nullable String eventValue) {
        //Do nothing
    }

    @Override
    public void reportJsInitEvent(@NonNull String value) {
        //Do nothing
    }

    @Override
    public void onWebViewReportingInit(@NonNull WebViewJsInterfaceHandler webViewJsInterfaceHandler) {
        //Do nothing
    }

    @Override
    public void registerAnrListener(@NonNull AnrListener listener) {
        //Do nothing
    }

    @Override
    public void reportExternalAttribution(@NonNull ExternalAttribution value) {
        // Do nothing
    }

    @Override
    public void reportUnhandledException(@NonNull PluginErrorDetails errorDetails) {
        //Do nothing
    }

    @Override
    public void reportError(@NonNull PluginErrorDetails errorDetails, @Nullable String message) {
        //Do nothing
    }

    @Override
    public void reportError(@NonNull String identifier,
                            @Nullable String message,
                            @Nullable PluginErrorDetails errorDetails) {
        //Do nothing
    }

    @Override
    public void addAutoCollectedDataSubscriber(@NonNull String subscriber) {
        // Do nothing
    }
}
