package io.appmetrica.analytics.impl.proxy;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.AdRevenue;
import io.appmetrica.analytics.ModuleEvent;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.Revenue;
import io.appmetrica.analytics.coreapi.internal.backport.Provider;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.crash.jvm.client.AllThreads;
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException;
import io.appmetrica.analytics.impl.proxy.synchronous.ReporterSynchronousStageExecutor;
import io.appmetrica.analytics.impl.proxy.validation.ReporterBarrier;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.plugins.IPluginReporter;
import io.appmetrica.analytics.profile.UserProfile;
import java.util.List;
import java.util.Map;

public class ReporterExtendedProxy implements IReporterExtended {

    private static final String TAG = "[ReporterExtendedProxy]";

    @NonNull
    private final AppMetricaFacadeProvider mProvider;
    @NonNull
    private final ReporterBarrier barrier;
    @NonNull
    private final ICommonExecutor mExecutor;
    @NonNull
    private final Context mContext;
    @NonNull
    private final ReporterConfig mConfigWithApiKeyOnly;
    @NonNull
    private final ReporterSynchronousStageExecutor synchronousStageExecutor;
    @NonNull
    private final PluginReporterProxy pluginReporterProxy;

    public ReporterExtendedProxy(@NonNull ICommonExecutor executor, @NonNull Context context, @NonNull String apiKey) {
        this(
                executor,
                context.getApplicationContext(),
                apiKey,
                new AppMetricaFacadeProvider()
        );
    }

    private ReporterExtendedProxy(@NonNull ICommonExecutor executor,
                                  @NonNull Context context,
                                  @NonNull String apiKey,
                                  @NonNull AppMetricaFacadeProvider provider) {
        this(
                context,
                new ReporterBarrier(),
                provider,
                new ReporterSynchronousStageExecutor(),
                ReporterConfig.newConfigBuilder(apiKey).build()
        );
    }

    private ReporterExtendedProxy(@NonNull final Context context,
                                  @NonNull ReporterBarrier barrier,
                                  @NonNull final AppMetricaFacadeProvider provider,
                                  @NonNull ReporterSynchronousStageExecutor synchronousStageExecutor,
                                  @NonNull final ReporterConfig config) {
        this(
                context,
                barrier,
                provider,
                synchronousStageExecutor,
                config,
                new PluginReporterProxy(
                    new Provider<IReporterExtended>() {
                            @Override
                            public IReporterExtended get() {
                                return getReporter(provider, context, config);
                            }
                        }
                )
        );
    }

    @VisibleForTesting
    ReporterExtendedProxy(@NonNull Context context,
                          @NonNull ReporterBarrier barrier,
                          @NonNull AppMetricaFacadeProvider provider,
                          @NonNull ReporterSynchronousStageExecutor synchronousStageExecutor,
                          @NonNull ReporterConfig config,
                          @NonNull PluginReporterProxy pluginReporterProxy) {
        mExecutor = ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor();
        mContext = context;
        this.barrier = barrier;
        mProvider = provider;
        this.synchronousStageExecutor = synchronousStageExecutor;
        mConfigWithApiKeyOnly = config;
        this.pluginReporterProxy = pluginReporterProxy;
    }

    @WorkerThread
    @NonNull
    final IReporterExtended getReporter() {
        return getReporter(mProvider, mContext, mConfigWithApiKeyOnly);
    }

    @WorkerThread
    @NonNull
    private static IReporterExtended getReporter(@NonNull AppMetricaFacadeProvider provider,
                                                 @NonNull Context context,
                                                 @NonNull ReporterConfig config) {
        return provider.getInitializedImpl(context).getReporter(config);
    }

    @Override
    public void reportUnhandledException(@NonNull final UnhandledException unhandledException) {
        barrier.reportUnhandledException(unhandledException);
        synchronousStageExecutor.reportUnhandledException(unhandledException);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportUnhandledException(unhandledException);
            }
        });
    }

    @Override
    public void reportAnr(@NonNull Map<Thread, StackTraceElement[]> allThreads) {
        barrier.reportAnr(allThreads);
        synchronousStageExecutor.reportAnr(allThreads);
        List<Map.Entry<Thread, StackTraceElement[]>> entries = CollectionUtils.getListFromMap(allThreads);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportAnr(CollectionUtils.getMapFromList(entries));
            }
        });
    }

    @Override
    public void reportAnr(@NonNull final AllThreads allThreads) {
        barrier.reportAnr(allThreads);
        synchronousStageExecutor.reportAnr(allThreads);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportAnr(allThreads);
            }
        });
    }

    @Override
    public void sendEventsBuffer() {
        barrier.sendEventsBuffer();
        synchronousStageExecutor.sendEventsBuffer();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().sendEventsBuffer();
            }
        });
    }

    @Override
    public void putAppEnvironmentValue(@NonNull final String key, @Nullable final String value) {
        barrier.putAppEnvironmentValue(key, value);
        synchronousStageExecutor.putAppEnvironmentValue(key, value);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().putAppEnvironmentValue(key, value);
            }
        });
    }

    @Override
    public void clearAppEnvironment() {
        barrier.clearAppEnvironment();
        synchronousStageExecutor.clearAppEnvironment();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().clearAppEnvironment();
            }
        });
    }

    @Override
    public void reportEvent(@NonNull final String eventName) {
        barrier.reportEvent(eventName);
        synchronousStageExecutor.reportEvent(eventName);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportEvent(eventName);
            }
        });
    }

    @Override
    public void reportEvent(@NonNull final String eventName, @Nullable final String jsonValue) {
        barrier.reportEvent(eventName, jsonValue);
        synchronousStageExecutor.reportEvent(eventName, jsonValue);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportEvent(eventName, jsonValue);
            }
        });
    }

    @Override
    public void reportEvent(@NonNull final String eventName, @Nullable Map<String, Object> attributes) {
        barrier.reportEvent(eventName, attributes);
        synchronousStageExecutor.reportEvent(eventName, attributes);
        final List<Map.Entry<String, Object>> entries = CollectionUtils.getListFromMap(attributes);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportEvent(eventName, CollectionUtils.getMapFromList(entries));
            }
        });
    }

    @Override
    public void reportError(@NonNull final String message, @Nullable final Throwable error) {
        barrier.reportError(message, error);
        final Throwable nonNullThrowable = synchronousStageExecutor.reportError(message, error);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportError(message, nonNullThrowable);
            }
        });
    }

    @Override
    public void reportError(@NonNull String identifier, @Nullable String message) {
        reportError(identifier, message, (Throwable) null);
    }

    @Override
    public void reportError(
            @NonNull final String identifier,
            @Nullable final String message,
            @Nullable final Throwable error
    ) {
        barrier.reportError(identifier, message, error);
        synchronousStageExecutor.reportError(identifier, message, error);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportError(identifier, message, error);
            }
        });
    }

    @Override
    public void reportUnhandledException(@NonNull final Throwable exception) {
        barrier.reportUnhandledException(exception);
        synchronousStageExecutor.reportUnhandledException(exception);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportUnhandledException(exception);
            }
        });
    }

    @Override
    public void resumeSession() {
        barrier.resumeSession();
        synchronousStageExecutor.resumeSession();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().resumeSession();
            }
        });
    }

    @Override
    public void pauseSession() {
        barrier.pauseSession();
        synchronousStageExecutor.pauseSession();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().pauseSession();
            }
        });
    }

    @Override
    public void setUserProfileID(@Nullable final String profileID) {
        barrier.setUserProfileID(profileID);
        synchronousStageExecutor.setUserProfileID(profileID);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().setUserProfileID(profileID);
            }
        });
    }

    @Override
    public void reportUserProfile(@NonNull final UserProfile profile) {
        barrier.reportUserProfile(profile);
        synchronousStageExecutor.reportUserProfile(profile);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportUserProfile(profile);
            }
        });
    }

    @Override
    public void reportRevenue(@NonNull final Revenue revenue) {
        barrier.reportRevenue(revenue);
        synchronousStageExecutor.reportRevenue(revenue);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportRevenue(revenue);
            }
        });
    }

    @Override
    public void reportAdRevenue(@NonNull final AdRevenue adRevenue) {
        barrier.reportAdRevenue(adRevenue);
        synchronousStageExecutor.reportAdRevenue(adRevenue);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportAdRevenue(adRevenue);
            }
        });
    }

    @Override
    public void reportECommerce(@NonNull final ECommerceEvent event) {
        barrier.reportECommerce(event);
        synchronousStageExecutor.reportECommerce(event);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportECommerce(event);
            }
        });
    }

    @Override
    public void setDataSendingEnabled(final boolean enabled) {
        barrier.setDataSendingEnabled(enabled);
        synchronousStageExecutor.setDataSendingEnabled(enabled);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().setDataSendingEnabled(enabled);
            }
        });
    }

    @NonNull
    @Override
    public IPluginReporter getPluginExtension() {
        return pluginReporterProxy;
    }

    public void activate(@NonNull final String apiKey) {
        final ReporterConfig config = ReporterConfig.newConfigBuilder(apiKey).build();
        barrier.activate(config);
        synchronousStageExecutor.activate(config);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                activateInternal(config);
            }
        });
    }

    public void activate(@NonNull final ReporterConfig config) {
        barrier.activate(config);
        synchronousStageExecutor.activate(config);
        DebugLogger.INSTANCE.info(TAG, "activate with apiKey: %s", config.apiKey);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                DebugLogger.INSTANCE.info(TAG, "activate internal with apiKey = %s", config.apiKey);
                activateInternal(config);
            }
        });
    }

    @WorkerThread
    private void activateInternal(@NonNull ReporterConfig config) {
        mProvider.getInitializedImpl(mContext).activateReporter(config);
    }

    @Override
    public void reportEvent(
        @NonNull final ModuleEvent moduleEvent
    ) {
        barrier.reportEvent(moduleEvent);
        synchronousStageExecutor.reportEvent(moduleEvent);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportEvent(moduleEvent);
            }
        });
    }

    @Override
    public void setSessionExtra(@NonNull final String key, @Nullable final byte[] value) {
        barrier.setSessionExtra(key, value);
        synchronousStageExecutor.setSessionExtra(key, value);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().setSessionExtra(key, value);
            }
        });
    }

    @Override
    public void reportAdRevenue(@NonNull final AdRevenue adRevenue, final boolean autoCollected) {
        barrier.reportAdRevenue(adRevenue, autoCollected);
        synchronousStageExecutor.reportAdRevenue(adRevenue, autoCollected);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportAdRevenue(adRevenue, autoCollected);
            }
        });
    }
}
