package io.appmetrica.analytics.impl.proxy;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
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
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.ActivityLifecycleManager;
import io.appmetrica.analytics.impl.AppMetricaFacade;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.ContextAppearedListener;
import io.appmetrica.analytics.impl.DefaultOneShotMetricaConfig;
import io.appmetrica.analytics.impl.IMainReporter;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.MainReporterApiConsumerProvider;
import io.appmetrica.analytics.impl.SdkUtils;
import io.appmetrica.analytics.impl.SessionsTrackingManager;
import io.appmetrica.analytics.impl.SynchronousStageExecutor;
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler;
import io.appmetrica.analytics.impl.proxy.validation.MainFacadeBarrier;
import io.appmetrica.analytics.impl.utils.ApiProxyThread;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.profile.UserProfile;
import java.util.List;
import java.util.Map;

public final class AppMetricaProxy extends BaseAppMetricaProxy {

    private static final String TAG = "[AppMetricaProxy]";

    @NonNull
    private final SilentActivationValidator silentActivationValidator;
    private final MainFacadeBarrier mMainFacadeBarrier;
    @NonNull
    private final SessionsTrackingManager sessionsTrackingManager;

    public AppMetricaProxy(@NonNull ICommonExecutor executor) {
        this(
                new AppMetricaFacadeProvider(),
                executor,
                new MainFacadeBarrier(),
                new WebViewJsInterfaceHandler()
        );
    }

    private AppMetricaProxy(@NonNull AppMetricaFacadeProvider provider,
                               @NonNull ICommonExecutor executor,
                               @NonNull MainFacadeBarrier mainFacadeBarrier,
                               @NonNull WebViewJsInterfaceHandler webViewJsInterfaceHandler) {
        this(
                provider,
                executor,
                mainFacadeBarrier,
                new ActivationValidator(provider),
                new SilentActivationValidator(provider),
                webViewJsInterfaceHandler,
                new SynchronousStageExecutor(provider, webViewJsInterfaceHandler),
                ReporterProxyStorage.getInstance(),
                ClientServiceLocator.getInstance().getDefaultOneShotConfig(),
                ClientServiceLocator.getInstance().getSessionsTrackingManager(),
                ClientServiceLocator.getInstance().getContextAppearedListener()
        );
    }

    @VisibleForTesting
    AppMetricaProxy(@NonNull AppMetricaFacadeProvider provider,
                       @NonNull ICommonExecutor executor,
                       @NonNull MainFacadeBarrier mainFacadeBarrier,
                       @NonNull ActivationValidator activationValidator,
                       @NonNull SilentActivationValidator silentActivationValidator,
                       @NonNull WebViewJsInterfaceHandler webViewJsInterfaceHandler,
                       @NonNull SynchronousStageExecutor synchronousStageExecutor,
                       @NonNull ReporterProxyStorage reporterProxyStorage,
                       @NonNull DefaultOneShotMetricaConfig defaultOneShotConfig,
                       @NonNull SessionsTrackingManager sessionsTrackingManager,
                       @NonNull ContextAppearedListener contextAppearedListener) {
        super(
                provider,
                executor,
                activationValidator,
                webViewJsInterfaceHandler,
                synchronousStageExecutor,
                reporterProxyStorage,
                defaultOneShotConfig,
                contextAppearedListener
        );
        mMainFacadeBarrier = mainFacadeBarrier;
        this.silentActivationValidator = silentActivationValidator;
        this.sessionsTrackingManager = sessionsTrackingManager;
    }

    public void activate(@NonNull final Context context, @NonNull final AppMetricaConfig config) {
        mMainFacadeBarrier.activate(context, config);
        getSynchronousStageExecutor().activate(context.getApplicationContext(), config);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().getInitializedImpl(context.getApplicationContext()).activateFull(
                        config,
                        getDefaultOneShotConfig().mergeWithUserConfig(config)
                );
            }
        });

        getProvider().markActivated();
    }

    public void sendEventsBuffer() {
        getActivationValidator().validate();
        mMainFacadeBarrier.sendEventsBuffer();
        getSynchronousStageExecutor().sendEventsBuffer();
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().sendEventsBuffer();
            }
        });
    }

    public void resumeSession(@Nullable final Activity activity) {
        getActivationValidator().validate();
        mMainFacadeBarrier.resumeSession();
        getSynchronousStageExecutor().resumeSession(activity);
        getExecutor().execute(new Runnable() {
            @Override
            @ApiProxyThread
            public void run() {
                sessionsTrackingManager.resumeActivityManually(activity, getMainReporter());
            }
        });
    }

    public void pauseSession(@Nullable final Activity activity) {
        getActivationValidator().validate();
        mMainFacadeBarrier.pauseSession();
        getSynchronousStageExecutor().pauseSession(activity);
        getExecutor().execute(new Runnable() {
            @Override
            @ApiProxyThread
            public void run() {
                sessionsTrackingManager.pauseActivityManually(activity, getMainReporter());
            }
        });
    }

    public void enableActivityAutoTracking(@NonNull final Application application) {
        getActivationValidator().validate();
        mMainFacadeBarrier.enableActivityAutoTracking(application);
        final ActivityLifecycleManager.WatchingStatus status = getSynchronousStageExecutor()
                .enableActivityAutoTracking(application);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().onEnableAutoTrackingAttemptOccurred(status);
            }
        });
    }

    public void reportEvent(@NonNull final String eventName) {
        getActivationValidator().validate();
        mMainFacadeBarrier.reportEvent(eventName);
        getSynchronousStageExecutor().reportEvent(eventName);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportEvent(eventName);
            }
        });
    }

    public void reportEvent(@NonNull final String eventName, @Nullable final String jsonValue) {
        getActivationValidator().validate();
        mMainFacadeBarrier.reportEvent(eventName, jsonValue);
        getSynchronousStageExecutor().reportEvent(eventName, jsonValue);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportEvent(eventName, jsonValue);
            }
        });
    }

    public void reportEvent(@NonNull final String eventName, @Nullable final Map<String, Object> attributes) {
        getActivationValidator().validate();
        mMainFacadeBarrier.reportEvent(eventName, attributes);
        getSynchronousStageExecutor().reportEvent(eventName, attributes);
        final List<Map.Entry<String, Object>> entries = CollectionUtils.getListFromMap(attributes);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportEvent(eventName, CollectionUtils.getMapFromList(entries));
            }
        });
    }

    public void reportError(@NonNull final String message, @Nullable final Throwable error) {
        getActivationValidator().validate();
        mMainFacadeBarrier.reportError(message, error);
        final Throwable nonNullError = getSynchronousStageExecutor().reportError(message, error);
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
        getActivationValidator().validate();
        mMainFacadeBarrier.reportError(identifier, message, error);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportError(identifier, message, error);
            }
        });
    }

    public void reportUnhandledException(@NonNull final Throwable exception) {
        getActivationValidator().validate();
        mMainFacadeBarrier.reportUnhandledException(exception);
        getSynchronousStageExecutor().reportUnhandledException(exception);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportUnhandledException(exception);
            }
        });
    }

    public void reportAppOpen(@NonNull final Activity activity) {
        getActivationValidator().validate();
        mMainFacadeBarrier.reportAppOpen(activity);
        final Intent openIntent = getSynchronousStageExecutor().reportAppOpen(activity);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporterApiConsumerProvider().getDeeplinkConsumer().reportAppOpen(openIntent);
            }
        });
    }

    public void reportAppOpen(@NonNull final String deeplink) {
        getActivationValidator().validate();
        mMainFacadeBarrier.reportAppOpen(deeplink);
        getSynchronousStageExecutor().reportAppOpen(deeplink);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporterApiConsumerProvider().getDeeplinkConsumer().reportAppOpen(deeplink);
            }
        });
    }

    public void reportAppOpen(@NonNull final Intent intent) {
        getActivationValidator().validate();
        mMainFacadeBarrier.reportAppOpen(intent);
        getSynchronousStageExecutor().reportAppOpen(intent);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporterApiConsumerProvider().getDeeplinkConsumer().reportAppOpen(intent);
            }
        });
    }

    public void reportReferralUrl(@NonNull final String referralUrl) {
        getActivationValidator().validate();
        mMainFacadeBarrier.reportReferralUrl(referralUrl);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportReferralUrl(referralUrl);
            }
        });
    }

    public void setLocation(@Nullable final Location location) {
        mMainFacadeBarrier.setLocation(location);
        getSynchronousStageExecutor().setLocation(location);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().setLocation(location);
            }
        });
    }

    public void setLocationTracking(final boolean enabled) {
        mMainFacadeBarrier.setLocationTracking(enabled);
        getSynchronousStageExecutor().setLocationTracking(enabled);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().setLocationTracking(enabled);
            }
        });
    }

    public void setDataSendingEnabled(final boolean enabled) {
        mMainFacadeBarrier.setDataSendingEnabled(enabled);
        getSynchronousStageExecutor().setDataSendingEnabled(enabled);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().setDataSendingEnabled(enabled);
            }
        });
    }

    public void setUserProfileID(@Nullable final String userProfileID) {
        mMainFacadeBarrier.setUserProfileID(userProfileID);
        getSynchronousStageExecutor().setUserProfileID(userProfileID);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().setUserProfileID(userProfileID);
            }
        });
    }

    public void reportUserProfile(@NonNull final UserProfile profile) {
        getActivationValidator().validate();
        mMainFacadeBarrier.reportUserProfile(profile);
        getSynchronousStageExecutor().reportUserProfile(profile);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportUserProfile(profile);
            }
        });
    }

    public void reportRevenue(@NonNull final Revenue revenue) {
        getActivationValidator().validate();
        mMainFacadeBarrier.reportRevenue(revenue);
        getSynchronousStageExecutor().reportRevenue(revenue);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportRevenue(revenue);
            }
        });
    }

    public void reportAdRevenue(@NonNull final AdRevenue adRevenue) {
        getActivationValidator().validate();
        mMainFacadeBarrier.reportAdRevenue(adRevenue);
        getSynchronousStageExecutor().reportAdRevenue(adRevenue);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportAdRevenue(adRevenue);
            }
        });
    }

    public void reportECommerce(@NonNull final ECommerceEvent event) {
        getActivationValidator().validate();
        mMainFacadeBarrier.reportECommerce(event);
        getSynchronousStageExecutor().reportECommerce(event);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportECommerce(event);
            }
        });
    }

    public void requestDeferredDeeplinkParameters(@NonNull final DeferredDeeplinkParametersListener listener) {
        getActivationValidator().validate();
        mMainFacadeBarrier.requestDeferredDeeplinkParameters(listener);
        getSynchronousStageExecutor().requestDeferredDeeplinkParameters(listener);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().peekInitializedImpl().requestDeferredDeeplinkParameters(listener);
            }
        });
    }

    public void requestDeferredDeeplink(@NonNull final DeferredDeeplinkListener listener) {
        getActivationValidator().validate();
        mMainFacadeBarrier.requestDeferredDeeplink(listener);
        getSynchronousStageExecutor().requestDeferredDeeplink(listener);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().peekInitializedImpl().requestDeferredDeeplink(listener);
            }
        });
    }

    @NonNull
    public IReporterExtended getReporter(@NonNull Context context, @NonNull String apiKey) {
        mMainFacadeBarrier.getReporter(context, apiKey);
        getContextAppearedListener().onProbablyAppeared(context.getApplicationContext());
        return getReporterProxyStorage().getOrCreate(context.getApplicationContext(), apiKey);
    }

    public void activateReporter(@NonNull Context context, @NonNull ReporterConfig config) {
        mMainFacadeBarrier.activateReporter(context, config);
        getSynchronousStageExecutor().activateReporter(context.getApplicationContext(), config);
        getReporterProxyStorage().getOrCreate(context.getApplicationContext(), config);
    }

    public void putErrorEnvironmentValue(@NonNull final String key, @Nullable final String value) {
        mMainFacadeBarrier.putErrorEnvironmentValue(key, value);
        getSynchronousStageExecutor().putErrorEnvironmentValue(key, value);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().putErrorEnvironmentValue(key, value);
            }
        });
    }

    public void initWebViewReporting(@NonNull WebView webView) {
        getActivationValidator().validate();
        mMainFacadeBarrier.initWebViewReporting(webView);
        getSynchronousStageExecutor().initWebViewReporting(webView, this);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().onWebViewReportingInit(getWebViewJsInterfaceHandler());
            }
        });
    }

    public void reportJsEvent(@NonNull final String eventName, @Nullable final String eventValue) {
        getActivationValidator().validate();
        if (!mMainFacadeBarrier.reportJsEvent(eventName, eventValue)) {
            Log.w(SdkUtils.APPMETRICA_TAG, "Impossible to report event because parameters are invalid.");
            return;
        }
        getSynchronousStageExecutor().reportJsEvent(eventName, eventValue);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().reportJsEvent(eventName, eventValue);
            }
        });
    }

    public void reportJsInitEvent(@NonNull final String value) {
        if (!silentActivationValidator.validate().isValid()) {
            YLogger.warning(TAG, "Impossible to report JS init event because AppMetrica has not been activated yet");
            return;
        }
        if (!mMainFacadeBarrier.reportJsInitEvent(value)) {
            YLogger.warning(TAG, "Impossible to report JS init event because value is invalid");
            return;
        }
        getSynchronousStageExecutor().reportJsInitEvent(value);
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
        mMainFacadeBarrier.getUuid(context);
        getSynchronousStageExecutor().getUuid(context.getApplicationContext());
        return ClientServiceLocator.getInstance().getMultiProcessSafeUuidProvider(context.getApplicationContext())
            .readUuid();
    }

    public void putAppEnvironmentValue(@NonNull final String key, @Nullable final String value) {
        mMainFacadeBarrier.putAppEnvironmentValue(key, value);
        getSynchronousStageExecutor().putAppEnvironmentValue(key, value);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().putAppEnvironmentValue(key, value);
            }
        });
    }

    public void clearAppEnvironment() {
        mMainFacadeBarrier.clearAppEnvironment();
        getSynchronousStageExecutor().clearAppEnvironment();
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
        mMainFacadeBarrier.requestStartupParams(context, callback, params);
        getSynchronousStageExecutor().requestStartupParams(context.getApplicationContext(), callback, params);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getProvider().getInitializedImpl(context.getApplicationContext())
                    .requestStartupParams(callback, params);
            }
        });
    }

    public void registerAnrListener(@NonNull final AnrListener listener) {
        getActivationValidator().validate();
        mMainFacadeBarrier.registerAnrListener(listener);
        getSynchronousStageExecutor().registerAnrListener(listener);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getMainReporter().registerAnrListener(listener);
            }
        });
    }

    @VisibleForTesting
    MainFacadeBarrier getMainFacadeBarrier() {
        return mMainFacadeBarrier;
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
