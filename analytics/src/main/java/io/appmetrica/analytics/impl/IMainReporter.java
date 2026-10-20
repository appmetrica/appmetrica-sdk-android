package io.appmetrica.analytics.impl;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AnrListener;
import io.appmetrica.analytics.ExternalAttribution;
import java.util.List;

public interface IMainReporter extends IBaseReporter, MetricaConfigurator {

    void reportAppOpen(@Nullable Intent intent);

    void reportAutoAppOpen(@Nullable String deeplink);

    void enableAnrMonitoring();
    
    void onEnableAutoTrackingAttemptOccurred(@NonNull ActivityLifecycleManager.WatchingStatus status);

    void resumeSession(@Nullable Activity activity);

    void pauseSession(@Nullable Activity activity);

    List<String> getCustomHosts();

    void onWebViewReportingInit(@NonNull WebViewJsInterfaceHandler webViewJsInterfaceHandler);

    void registerAnrListener(@NonNull AnrListener listener);

    void reportExternalAttribution(@NonNull final ExternalAttribution value);
}
