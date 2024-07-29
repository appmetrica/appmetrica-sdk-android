package io.appmetrica.analytics.impl.proxy;

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
import io.appmetrica.analytics.ExternalAttribution;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.Revenue;
import io.appmetrica.analytics.StartupParamsCallback;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.ActivityLifecycleManager;
import io.appmetrica.analytics.impl.AppMetricaFacade;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.DefaultOneShotMetricaConfig;
import io.appmetrica.analytics.impl.IMainReporter;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.MainReporterApiConsumerProvider;
import io.appmetrica.analytics.impl.SdkUtils;
import io.appmetrica.analytics.impl.SessionsTrackingManager;
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler;
import io.appmetrica.analytics.impl.proxy.synchronous.SynchronousStageExecutor;
import io.appmetrica.analytics.impl.proxy.validation.Barrier;
import io.appmetrica.analytics.impl.proxy.validation.SilentActivationValidator;
import io.appmetrica.analytics.impl.utils.ApiProxyThread;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueProcessor;
import io.appmetrica.analytics.profile.UserProfile;
import java.util.List;
import java.util.Map;

public final class AppMetricaProxy extends BaseAppMetricaProxy {

    private static final String TAG = "[AppMetricaProxy]";

    @NonNull
    private final SilentActivationValidator silentActivationValidator;
    @NonNull
    private final Barrier barrier;
    @NonNull
    private final SynchronousStageExecutor synchronousStageExecutor;
    @NonNull
    private final SessionsTrackingManager sessionsTrackingManager;

    public AppMetricaProxy() {
        this(
            ClientServiceLocator.getInstance().getAppMetricaFacadeProvider(),
            new WebViewJsInterfaceHandler()
        );
    }

    private AppMetricaProxy(
        @NonNull AppMetricaFacadeProvider provider,
        @NonNull WebViewJsInterfaceHandler webViewJsInterfaceHandler
    ) {
        this(
            provider,
            new Barrier(provider),
            new SilentActivationValidator(provider),
            webViewJsInterfaceHandler,
            new SynchronousStageExecutor(provider, webViewJsInterfaceHandler),
            ReporterProxyStorage.getInstance(),
            ClientServiceLocator.getInstance().getDefaultOneShotConfig(),
            ClientServiceLocator.getInstance().getSessionsTrackingManager()
        );
    }

    @VisibleForTesting
    AppMetricaProxy(
        @NonNull AppMetricaFacadeProvider provider,
        @NonNull Barrier barrier,
        @NonNull SilentActivationValidator silentActivationValidator,
        @NonNull WebViewJsInterfaceHandler webViewJsInterfaceHandler,
        @NonNull SynchronousStageExecutor synchronousStageExecutor,
        @NonNull ReporterProxyStorage reporterProxyStorage,
        @NonNull DefaultOneShotMetricaConfig defaultOneShotConfig,
        @NonNull SessionsTrackingManager sessionsTrackingManager
    ) {
        super(
            provider,
            webViewJsInterfaceHandler,
            reporterProxyStorage,
            defaultOneShotConfig
        );
        this.barrier = barrier;
        this.synchronousStageExecutor = synchronousStageExecutor;
        this.silentActivationValidator = silentActivationValidator;
        this.sessionsTrackingManager = sessionsTrackingManager;
    }

    public void activate(@NonNull final Context context, @NonNull final AppMetricaConfig config) {
        barrier.activate(context, config);
        synchronousStageExecutor.activate(context.getApplicationContext(), config);
        getExecutor().execute(() -> getProvider().getInitializedImpl(context.getApplicationContext()).activateFull(
                getDefaultOneShotConfig().mergeWithUserConfig(config)
        ));

        getProvider().markActivated();
    }

    public void sendEventsBuffer() {
        barrier.sendEventsBuffer();
        synchronousStageExecutor.sendEventsBuffer();
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().sendEventsBuffer();
            }
        });
    }

    public void resumeSession(@Nullable final Activity activity) {
        barrier.resumeSession();
        synchronousStageExecutor.resumeSession(activity);
        getExecutor().execute(new Runnable() {
            @Override
            @ApiProxyThread
            public void run() {
                sessionsTrackingManager.resumeActivityManually(activity, getMainReporter());
            }
        });
    }

    public void pauseSession(@Nullable final Activity activity) {
        barrier.pauseSession();
        synchronousStageExecutor.pauseSession(activity);
        getExecutor().execute(new Runnable() {
            @Override
            @ApiProxyThread
            public void run() {
                sessionsTrackingManager.pauseActivityManually(activity, getMainReporter());
            }
        });
    }

    public void enableActivityAutoTracking(@NonNull final Application application) {
        barrier.enableActivityAutoTracking(application);
        synchronousStageExecutor.enableActivityAutoTracking(application);
        getExecutor().execute(() -> {
            final ActivityLifecycleManager.WatchingStatus status = sessionsTrackingManager.startWatchingIfNotYet();
            getMainReporter().onEnableAutoTrackingAttemptOccurred(status);
        });
    }

    public void reportEvent(@NonNull final String eventName) {
        barrier.reportEvent(eventName);
        synchronousStageExecutor.reportEvent(eventName);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportEvent(eventName);
            }
        });
    }

    public void reportEvent(@NonNull final String eventName, @Nullable final String jsonValue) {
        barrier.reportEvent(eventName, jsonValue);
        synchronousStageExecutor.reportEvent(eventName, jsonValue);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportEvent(eventName, jsonValue);
            }
        });
    }

    public void reportEvent(@NonNull final String eventName, @Nullable final Map<String, Object> attributes) {
        barrier.reportEvent(eventName, attributes);
        synchronousStageExecutor.reportEvent(eventName, attributes);
        final List<Map.Entry<String, Object>> entries = CollectionUtils.getListFromMap(attributes);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportEvent(eventName, CollectionUtils.getMapFromList(entries));
            }
        });
    }

    public void reportError(@NonNull final String message, @Nullable final Throwable error) {
        barrier.reportError(message, error);
        final Throwable nonNullError = synchronousStageExecutor.reportError(message, error);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportError(message, nonNullError);
            }
        });
    }

    public void reportError(
            @NonNull final String identifier,
            @Nullable final String message,
            @Nullable final Throwable error
    ) {
        barrier.reportError(identifier, message, error);
        synchronousStageExecutor.reportError(identifier, message, error);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportError(identifier, message, error);
            }
        });
    }

    public void reportUnhandledException(@NonNull final Throwable exception) {
        barrier.reportUnhandledException(exception);
        synchronousStageExecutor.reportUnhandledException(exception);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportUnhandledException(exception);
            }
        });
    }

    public void reportAppOpen(@NonNull final Activity activity) {
        barrier.reportAppOpen(activity);
        final Intent openIntent = synchronousStageExecutor.reportAppOpen(activity);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporterApiConsumerProvider().getDeeplinkConsumer().reportAppOpen(openIntent);
            }
        });
    }

    public void reportAppOpen(@NonNull final String deeplink) {
        barrier.reportAppOpen(deeplink);
        synchronousStageExecutor.reportAppOpen(deeplink);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporterApiConsumerProvider().getDeeplinkConsumer().reportAppOpen(deeplink);
            }
        });
    }

    public void reportAppOpen(@NonNull final Intent intent) {
        barrier.reportAppOpen(intent);
        synchronousStageExecutor.reportAppOpen(intent);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporterApiConsumerProvider().getDeeplinkConsumer().reportAppOpen(intent);
            }
        });
    }

    public void reportReferralUrl(@NonNull final String referralUrl) {
        barrier.reportReferralUrl(referralUrl);
        synchronousStageExecutor.reportReferralUrl(referralUrl);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportReferralUrl(referralUrl);
            }
        });
    }

    public void setLocation(@Nullable final Location location) {
        barrier.setLocation(location);
        synchronousStageExecutor.setLocation(location);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().setLocation(location);
            }
        });
    }

    public void setLocationTracking(final boolean enabled) {
        barrier.setLocationTracking(enabled);
        synchronousStageExecutor.setLocationTracking(enabled);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().setLocationTracking(enabled);
            }
        });
    }

    public void setDataSendingEnabled(final boolean enabled) {
        barrier.setDataSendingEnabled(enabled);
        synchronousStageExecutor.setDataSendingEnabled(enabled);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().setDataSendingEnabled(enabled);
            }
        });
    }

    public void setUserProfileID(@Nullable final String userProfileID) {
        barrier.setUserProfileID(userProfileID);
        synchronousStageExecutor.setUserProfileID(userProfileID);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().setUserProfileID(userProfileID);
            }
        });
    }

    public void reportUserProfile(@NonNull final UserProfile profile) {
        barrier.reportUserProfile(profile);
        synchronousStageExecutor.reportUserProfile(profile);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportUserProfile(profile);
            }
        });
    }

    public void reportRevenue(@NonNull final Revenue revenue) {
        barrier.reportRevenue(revenue);
        synchronousStageExecutor.reportRevenue(revenue);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportRevenue(revenue);
            }
        });
    }

    public void reportAdRevenue(@NonNull final AdRevenue adRevenue) {
        barrier.reportAdRevenue(adRevenue);
        synchronousStageExecutor.reportAdRevenue(adRevenue);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportAdRevenue(adRevenue);
            }
        });
    }

    public void reportECommerce(@NonNull final ECommerceEvent event) {
        barrier.reportECommerce(event);
        synchronousStageExecutor.reportECommerce(event);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportECommerce(event);
            }
        });
    }

    public void requestDeferredDeeplinkParameters(@NonNull final DeferredDeeplinkParametersListener listener) {
        barrier.requestDeferredDeeplinkParameters(listener);
        synchronousStageExecutor.requestDeferredDeeplinkParameters(listener);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().peekInitializedImpl().requestDeferredDeeplinkParameters(listener);
            }
        });
    }

    public void requestDeferredDeeplink(@NonNull final DeferredDeeplinkListener listener) {
        barrier.requestDeferredDeeplink(listener);
        synchronousStageExecutor.requestDeferredDeeplink(listener);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().peekInitializedImpl().requestDeferredDeeplink(listener);
            }
        });
    }

    @NonNull
    public IReporterExtended getReporter(@NonNull Context context, @NonNull String apiKey) {
        barrier.getReporter(context, apiKey);
        synchronousStageExecutor.getReporter(context.getApplicationContext(), apiKey);
        return getReporterProxyStorage().getOrCreate(context.getApplicationContext(), apiKey);
    }

    public void activateReporter(@NonNull Context context, @NonNull ReporterConfig config) {
        DebugLogger.INSTANCE.info(TAG, "activate reporter with apiKey = %s", config.apiKey);
        barrier.activateReporter(context, config);
        synchronousStageExecutor.activateReporter(context.getApplicationContext(), config);
        getReporterProxyStorage().getOrCreate(context.getApplicationContext(), config);
    }

    public void putErrorEnvironmentValue(@NonNull final String key, @Nullable final String value) {
        barrier.putErrorEnvironmentValue(key, value);
        synchronousStageExecutor.putErrorEnvironmentValue(key, value);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().putErrorEnvironmentValue(key, value);
            }
        });
    }

    public void initWebViewReporting(@NonNull WebView webView) {
        barrier.initWebViewReporting(webView);
        synchronousStageExecutor.initWebViewReporting(webView, this);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().onWebViewReportingInit(getWebViewJsInterfaceHandler());
            }
        });
    }

    public void reportJsEvent(@NonNull final String eventName, @Nullable final String eventValue) {
        if (!barrier.reportJsEvent(eventName, eventValue)) {
            DebugLogger.INSTANCE.warning(
                SdkUtils.APPMETRICA_TAG,
                "Impossible to report event because parameters are invalid."
            );
            return;
        }
        synchronousStageExecutor.reportJsEvent(eventName, eventValue);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportJsEvent(eventName, eventValue);
            }
        });
    }

    public void reportJsInitEvent(@NonNull final String value) {
        if (!silentActivationValidator.validate().isValid()) {
            DebugLogger.INSTANCE.warning(
                TAG,
                "Impossible to report JS init event because AppMetrica has not been activated yet"
            );
            return;
        }
        if (!barrier.reportJsInitEvent(value)) {
            DebugLogger.INSTANCE.warning(TAG, "Impossible to report JS init event because value is invalid");
            return;
        }
        synchronousStageExecutor.reportJsInitEvent(value);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportJsInitEvent(value);
            }
        });
    }

    @Nullable
    public String getDeviceId() {
        final AppMetricaFacade appMetrica = getProvider().peekInitializedImpl();
        return appMetrica == null ? null : appMetrica.getDeviceId();
    }

    @NonNull
    public IdentifiersResult getUuid(@NonNull Context context) {
        barrier.getUuid(context);
        synchronousStageExecutor.getUuid(context.getApplicationContext());
        return ClientServiceLocator.getInstance().getMultiProcessSafeUuidProvider(context.getApplicationContext())
            .readUuid();
    }

    public void putAppEnvironmentValue(@NonNull final String key, @Nullable final String value) {
        barrier.putAppEnvironmentValue(key, value);
        synchronousStageExecutor.putAppEnvironmentValue(key, value);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().putAppEnvironmentValue(key, value);
            }
        });
    }

    public void clearAppEnvironment() {
        barrier.clearAppEnvironment();
        synchronousStageExecutor.clearAppEnvironment();
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().clearAppEnvironment();
            }
        });
    }

    public void requestStartupParams(
            @NonNull final Context context,
            @NonNull final StartupParamsCallback callback,
            @NonNull final List<String> params
    ) {
        barrier.requestStartupParams(context, callback, params);
        synchronousStageExecutor.requestStartupParams(context.getApplicationContext(), callback, params);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().getInitializedImpl(context.getApplicationContext())
                    .requestStartupParams(callback, params);
            }
        });
    }

    public void registerAnrListener(@NonNull final AnrListener listener) {
        barrier.registerAnrListener(listener);
        synchronousStageExecutor.registerAnrListener(listener);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().registerAnrListener(listener);
            }
        });
    }

    public void reportExternalAttribution(@NonNull final ExternalAttribution value) {
        barrier.reportExternalAttribution(value);
        synchronousStageExecutor.reportExternalAttribution(value);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportExternalAttribution(value);
            }
        });
    }

    public void reportExternalAdRevenue(@NonNull Object... values) {
        barrier.reportExternalAdRevenue(values);
        synchronousStageExecutor.reportExternalAdRevenue(values);
        getExecutor().execute(() -> {
            ModuleAdRevenueProcessor processor =
                ClientServiceLocator.getInstance().getModulesController().getModuleAdRevenueProcessor();
            if (processor != null) {
                processor.process(values);
            }
        });
    }

    @VisibleForTesting
    Barrier getMainFacadeBarrier() {
        return barrier;
    }

    @NonNull
    private IMainReporter getMainReporter() {
        return getMainReporterApiConsumerProvider().getMainReporter();
    }

    @NonNull
    private MainReporterApiConsumerProvider getMainReporterApiConsumerProvider() {
        return getProvider().peekInitializedImpl().getMainReporterApiConsumerProvider();
    }
}
