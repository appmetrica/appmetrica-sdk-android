package io.appmetrica.analytics.impl;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.AdRevenue;
import io.appmetrica.analytics.AnrListener;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.Revenue;
import io.appmetrica.analytics.StartupParamsCallback;
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.crash.AppMetricaThrowable;
import io.appmetrica.analytics.impl.crash.client.AllThreads;
import io.appmetrica.analytics.impl.crash.client.UnhandledException;
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider;
import io.appmetrica.analytics.impl.proxy.AppMetricaProxy;
import io.appmetrica.analytics.impl.utils.LoggerStorage;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.plugins.PluginErrorDetails;
import io.appmetrica.analytics.profile.UserProfile;
import java.util.List;
import java.util.Map;

public class SynchronousStageExecutor {

    @NonNull
    private final AppMetricaFacadeProvider mProvider;
    @NonNull
    private final WebViewJsInterfaceHandler webViewJsInterfaceHandler;
    @NonNull
    private final ActivityLifecycleManager activityLifecycleManager;
    @NonNull
    private final SessionsTrackingManager sessionsTrackingManager;
    @NonNull
    private final ContextAppearedListener contextAppearedListener;

    public SynchronousStageExecutor(@NonNull AppMetricaFacadeProvider provider,
                                    @NonNull WebViewJsInterfaceHandler webViewJsInterfaceHandler) {
        this(
            provider,
            webViewJsInterfaceHandler,
            ClientServiceLocator.getInstance().getActivityLifecycleManager(),
            ClientServiceLocator.getInstance().getSessionsTrackingManager(),
            ClientServiceLocator.getInstance().getContextAppearedListener()
        );
    }

    @VisibleForTesting
    public SynchronousStageExecutor(@NonNull AppMetricaFacadeProvider provider,
                                    @NonNull WebViewJsInterfaceHandler webViewJsInterfaceHandler,
                                    @NonNull ActivityLifecycleManager activityLifecycleManager,
                                    @NonNull SessionsTrackingManager sessionsTrackingManager,
                                    @NonNull ContextAppearedListener contextAppearedListener) {
        mProvider = provider;
        this.webViewJsInterfaceHandler = webViewJsInterfaceHandler;
        this.activityLifecycleManager = activityLifecycleManager;
        this.sessionsTrackingManager = sessionsTrackingManager;
        this.contextAppearedListener = contextAppearedListener;
    }

    public void putAppEnvironmentValue(@NonNull String key, @Nullable String value) {

    }

    public void clearAppEnvironment() {

    }

    public void sendEventsBuffer() {

    }

    public void reportEvent(@NonNull String eventName) {

    }

    public void reportEvent(@NonNull String eventName, @Nullable String jsonValue) {

    }

    public void reportEvent(@NonNull String eventName, @Nullable Map<String, Object> attributes) {

    }

    @NonNull
    public Throwable reportError(@NonNull String message, @Nullable Throwable error) {
        if (error == null) {
            Throwable nonNullError = new AppMetricaThrowable();
            nonNullError.fillInStackTrace();
            return nonNullError;
        } else {
            return error;
        }
    }

    public void reportUnhandledException(@NonNull Throwable exception) {

    }

    public void resumeSession(@Nullable Activity activity) {

    }

    public void pauseSession(@NonNull Activity activity) {

    }

    public void resumeSession() {

    }

    public void pauseSession() {

    }

    public void setUserProfileID(@Nullable String profileID) {

    }

    public void reportUserProfile(@NonNull UserProfile profile) {

    }

    public void reportRevenue(@NonNull Revenue revenue) {

    }

    public void reportAdRevenue(@NonNull AdRevenue adRevenue) {

    }

    public void reportECommerce(@NonNull ECommerceEvent event) {

    }

    public void setStatisticsSending(boolean enabled) {

    }

    public void activate(@NonNull final Context context, @NonNull final AppMetricaConfig config) {
        activateInternal(context, config);
    }

    public void activate(@NonNull final ReporterConfig config) {

    }

    public void activate(@NonNull final String apiKey) {

    }

    @NonNull
    public ActivityLifecycleManager.WatchingStatus enableActivityAutoTracking(@NonNull Application application) {
        activityLifecycleManager.maybeInit(application);
        return sessionsTrackingManager.startWatching(false);
    }

    @Nullable
    public Intent reportAppOpen(@Nullable Activity activity) {
        return SystemServiceUtils.accessSystemServiceSafely(
            activity,
            "getting intent",
            "activity",
            new FunctionWithThrowable<Activity, Intent>() {
                @Override
                public Intent apply(@NonNull Activity input) throws Throwable {
                    return input.getIntent();
                }
            }
        );
    }

    public void reportAppOpen(@NonNull final String deeplink) {

    }

    public void reportAppOpen(@NonNull Intent intent) {

    }

    public void setLocation(@Nullable final Location location) {

    }

    public void setLocationTracking(final boolean enabled) {

    }

    public void setStatisticsSending(@NonNull final Context context, boolean enabled) {
        contextAppearedListener.onProbablyAppeared(context);
    }

    public void requestDeferredDeeplinkParameters(@NonNull final DeferredDeeplinkParametersListener listener) {

    }

    public void requestDeferredDeeplink(@NonNull final DeferredDeeplinkListener listener) {

    }

    public void initialize(@NonNull final Context context) {
        contextAppearedListener.onProbablyAppeared(context);
    }

    public void initialize(@NonNull final Context context, @NonNull final AppMetricaConfig config) {
        activateInternal(context, config);
    }

    private void activateInternal(@NonNull final Context context, @NonNull final AppMetricaConfig config) {
        contextAppearedListener.onProbablyAppeared(context);
        final PublicLogger logger = LoggerStorage.getOrCreatePublicLogger(config.apiKey);
        if (WrapUtils.getOrDefault(
            config.sessionsAutoTrackingEnabled,
            DefaultValues.DEFAULT_SESSIONS_AUTO_TRACKING_ENABLED
        )) {
            if (logger.isEnabled()) {
                logger.i("Session auto tracking enabled");
            }
            sessionsTrackingManager.startWatching(true);
        } else {
            if (logger.isEnabled()) {
                logger.i("Session auto tracking disabled");
            }
        }
        mProvider.getInitializedImpl(context).activateCore(config);
    }

    public void putErrorEnvironmentValue(@NonNull final String key, @Nullable final String value) {

    }

    public void enableAnrMonitoring() {

    }

    public void activateReporter(@NonNull Context context, @NonNull ReporterConfig config) {
        contextAppearedListener.onProbablyAppeared(context);
    }

    public void reportUnhandledException(@NonNull UnhandledException unhandledException) {

    }

    public void reportAnr(@NonNull AllThreads allThreads) {

    }

    public void requestStartupParams(
        @NonNull final Context context,
        @NonNull final StartupParamsCallback callback,
        @NonNull final List<String> params
    ) {
        contextAppearedListener.onProbablyAppeared(context);
    }

    public void initWebViewReporting(@NonNull WebView webView, @NonNull AppMetricaProxy proxy) {
        webViewJsInterfaceHandler.initWebViewReporting(webView, proxy);
    }

    public void reportJsEvent(@NonNull String eventName, @Nullable String eventValue) {

    }

    public void reportJsInitEvent(@NonNull String value) {

    }

    public void reportPluginUnhandledException(@NonNull PluginErrorDetails errorDetails) {

    }

    public void reportPluginError(@NonNull PluginErrorDetails errorDetails, @Nullable String message) {

    }

    public void reportPluginError(@NonNull String identifier,
                                  @Nullable String message,
                                  @Nullable PluginErrorDetails errorDetails) {

    }

    public void getFeatures(@NonNull Context context) {
        contextAppearedListener.onProbablyAppeared(context);
    }

    public void getUuid(@NonNull Context context) {
        contextAppearedListener.onProbablyAppeared(context);
    }

    public void registerAnrListener(@NonNull AnrListener listener) {

    }
}
