package io.appmetrica.analytics.impl;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AnrListener;
import io.appmetrica.analytics.ExternalAttribution;
import java.util.List;

public interface IMainReporter extends IBaseReporter, MetricaConfigurator {

    void enableAnrMonitoring();
    
    void reportAppOpen(@NonNull String deeplink, boolean auto);

    void reportReferralUrl(@NonNull String referralUrl);

    void onEnableAutoTrackingAttemptOccurred(@NonNull ActivityLifecycleManager.WatchingStatus status);

    void resumeSession(@Nullable Activity activity);

    void pauseSession(@Nullable Activity activity);

    List<String> getCustomHosts();

    void onWebViewReportingInit(@NonNull WebViewJsInterfaceHandler webViewJsInterfaceHandler);

    void registerAnrListener(@NonNull AnrListener listener);

    void reportExternalAttribution(@NonNull final ExternalAttribution value);
}
