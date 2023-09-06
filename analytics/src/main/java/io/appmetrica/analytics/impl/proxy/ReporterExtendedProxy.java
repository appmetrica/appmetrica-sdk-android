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
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.SynchronousStageExecutor;
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler;
import io.appmetrica.analytics.impl.crash.client.AllThreads;
import io.appmetrica.analytics.impl.crash.client.UnhandledException;
import io.appmetrica.analytics.impl.proxy.validation.ReporterBarrier;
import io.appmetrica.analytics.plugins.IPluginReporter;
import io.appmetrica.analytics.profile.UserProfile;
import java.util.List;
import java.util.Map;

public class ReporterExtendedProxy implements IReporterExtended {

    @NonNull
    private final AppMetricaFacadeProvider mProvider;
    @NonNull
    private final ReporterBarrier mBarrier;
    @NonNull
    private final ICommonExecutor mExecutor;
    @NonNull
    private final Context mContext;
    @NonNull
    private final ReporterConfig mConfigWithApiKeyOnly;
    @NonNull
    private final SynchronousStageExecutor mSynchronousStageExecutor;
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
                executor,
                context,
                new ReporterBarrier(),
                provider,
                new SynchronousStageExecutor(provider, new WebViewJsInterfaceHandler()),
                ReporterConfig.newConfigBuilder(apiKey).build()
        );
    }

    private ReporterExtendedProxy(@NonNull ICommonExecutor executor,
                                  @NonNull final Context context,
                                  @NonNull ReporterBarrier barrier,
                                  @NonNull final AppMetricaFacadeProvider provider,
                                  @NonNull SynchronousStageExecutor synchronousStageExecutor,
                                  @NonNull final ReporterConfig config) {
        this(
                executor,
                context,
                barrier,
                provider,
                synchronousStageExecutor,
                config,
                new PluginReporterProxy(
                        barrier.getPluginExtension(),
                        synchronousStageExecutor,
                        executor,
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
    ReporterExtendedProxy(@NonNull ICommonExecutor executor,
                          @NonNull Context context,
                          @NonNull ReporterBarrier barrier,
                          @NonNull AppMetricaFacadeProvider provider,
                          @NonNull SynchronousStageExecutor synchronousStageExecutor,
                          @NonNull ReporterConfig config,
                          @NonNull PluginReporterProxy pluginReporterProxy) {
        mExecutor = executor;
        mContext = context;
        mBarrier = barrier;
        mProvider = provider;
        mSynchronousStageExecutor = synchronousStageExecutor;
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
        mSynchronousStageExecutor.reportUnhandledException(unhandledException);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportUnhandledException(unhandledException);
            }
        });
    }

    @Override
    public void reportAnr(@NonNull final AllThreads allThreads) {
        mSynchronousStageExecutor.reportAnr(allThreads);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportAnr(allThreads);
            }
        });
    }

    @Override
    public void sendEventsBuffer() {
        mBarrier.sendEventsBuffer();
        mSynchronousStageExecutor.sendEventsBuffer();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().sendEventsBuffer();
            }
        });
    }

    @Override
    public void putAppEnvironmentValue(@NonNull final String key, @Nullable final String value) {
        mBarrier.putAppEnvironmentValue(key, value);
        mSynchronousStageExecutor.putAppEnvironmentValue(key, value);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().putAppEnvironmentValue(key, value);
            }
        });
    }

    @Override
    public void clearAppEnvironment() {
        mBarrier.clearAppEnvironment();
        mSynchronousStageExecutor.clearAppEnvironment();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().clearAppEnvironment();
            }
        });
    }

    @Override
    public void reportEvent(@NonNull final String eventName) {
        mBarrier.reportEvent(eventName);
        mSynchronousStageExecutor.reportEvent(eventName);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportEvent(eventName);
            }
        });
    }

    @Override
    public void reportEvent(@NonNull final String eventName, @Nullable final String jsonValue) {
        mBarrier.reportEvent(eventName, jsonValue);
        mSynchronousStageExecutor.reportEvent(eventName, jsonValue);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportEvent(eventName, jsonValue);
            }
        });
    }

    @Override
    public void reportEvent(@NonNull final String eventName, @Nullable Map<String, Object> attributes) {
        mBarrier.reportEvent(eventName, attributes);
        mSynchronousStageExecutor.reportEvent(eventName, attributes);
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
        mBarrier.reportError(message, error);
        final Throwable nonNullThrowable = mSynchronousStageExecutor.reportError(message, error);
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
        mBarrier.reportError(identifier, message, error);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportError(identifier, message, error);
            }
        });
    }

    @Override
    public void reportUnhandledException(@NonNull final Throwable exception) {
        mBarrier.reportUnhandledException(exception);
        mSynchronousStageExecutor.reportUnhandledException(exception);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportUnhandledException(exception);
            }
        });
    }

    @Override
    public void resumeSession() {
        mBarrier.resumeSession();
        mSynchronousStageExecutor.resumeSession();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().resumeSession();
            }
        });
    }

    @Override
    public void pauseSession() {
        mBarrier.pauseSession();
        mSynchronousStageExecutor.pauseSession();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().pauseSession();
            }
        });
    }

    @Override
    public void setUserProfileID(@Nullable final String profileID) {
        mBarrier.setUserProfileID(profileID);
        mSynchronousStageExecutor.setUserProfileID(profileID);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().setUserProfileID(profileID);
            }
        });
    }

    @Override
    public void reportUserProfile(@NonNull final UserProfile profile) {
        mBarrier.reportUserProfile(profile);
        mSynchronousStageExecutor.reportUserProfile(profile);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportUserProfile(profile);
            }
        });
    }

    @Override
    public void reportRevenue(@NonNull final Revenue revenue) {
        mBarrier.reportRevenue(revenue);
        mSynchronousStageExecutor.reportRevenue(revenue);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportRevenue(revenue);
            }
        });
    }

    @Override
    public void reportAdRevenue(@NonNull final AdRevenue adRevenue) {
        mBarrier.reportAdRevenue(adRevenue);
        mSynchronousStageExecutor.reportAdRevenue(adRevenue);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportAdRevenue(adRevenue);
            }
        });
    }

    @Override
    public void reportECommerce(@NonNull final ECommerceEvent event) {
        mBarrier.reportECommerce(event);
        mSynchronousStageExecutor.reportECommerce(event);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportECommerce(event);
            }
        });
    }

    @Override
    public void setDataSendingEnabled(final boolean enabled) {
        mBarrier.setDataSendingEnabled(enabled);
        mSynchronousStageExecutor.setDataSendingEnabled(enabled);
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
        mSynchronousStageExecutor.activate(config);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                activateInternal(config);
            }
        });
    }

    public void activate(@NonNull final ReporterConfig config) {
        mSynchronousStageExecutor.activate(config);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
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
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().reportEvent(moduleEvent);
            }
        });
    }

    @Override
    public void setSessionExtra(@NonNull final String key, @Nullable final byte[] value) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getReporter().setSessionExtra(key, value);
            }
        });
    }
}
