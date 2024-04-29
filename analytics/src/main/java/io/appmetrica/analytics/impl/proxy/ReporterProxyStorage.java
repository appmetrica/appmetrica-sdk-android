package io.appmetrica.analytics.impl.proxy;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.HashMap;
import java.util.Map;

public class ReporterProxyStorage {

    private static final String TAG = "[ReporterProxyStorage]";

    private static class Holder {
        private static final ReporterProxyStorage sInstance = new ReporterProxyStorage(
                new AppMetricaFacadeProvider()
        );
    }

    private final Map<String, ReporterExtendedProxy> mReportersProxies =
            new HashMap<String, ReporterExtendedProxy>();
    @NonNull
    private final AppMetricaFacadeProvider mFacadeProvider;

    @NonNull
    public static ReporterProxyStorage getInstance() {
        return Holder.sInstance;
    }

    @VisibleForTesting
    ReporterProxyStorage(@NonNull AppMetricaFacadeProvider provider) {
        mFacadeProvider = provider;
    }

    @NonNull
    private ReporterExtendedProxy prepareImplAndCreate(@NonNull final Context context, @NonNull String apiKey) {
        YLogger.info(TAG, "prepareImplAndCreate");
        ICommonExecutor executor = ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor();
        if (mFacadeProvider.peekInitializedImpl() == null) {
            YLogger.info(TAG, "needs warm up core");
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    YLogger.info(TAG, "getInitializedImpl");
                    mFacadeProvider.getInitializedImpl(context);
                }
            });
        }
        ReporterExtendedProxy proxy = new ReporterExtendedProxy(executor, context, apiKey);
        mReportersProxies.put(apiKey, proxy);
        return proxy;
    }

    @NonNull
    public ReporterExtendedProxy getOrCreate(@NonNull final Context context, @NonNull String apiKey) {
        ReporterExtendedProxy proxy = mReportersProxies.get(apiKey);
        if (proxy == null) {
            synchronized (mReportersProxies) {
                proxy = mReportersProxies.get(apiKey);
                if (proxy == null) {
                    proxy = prepareImplAndCreate(context, apiKey);
                    proxy.activate(apiKey);
                }
            }
        }
        return proxy;
    }

    @NonNull
    public ReporterExtendedProxy getOrCreate(@NonNull final Context context, @NonNull ReporterConfig config) {
        YLogger.info(TAG, "getOrCreate");
        ReporterExtendedProxy proxy = mReportersProxies.get(config.apiKey);
        if (proxy == null) {
            YLogger.info(TAG, "needs create proxy");
            synchronized (mReportersProxies) {
                proxy = mReportersProxies.get(config.apiKey);
                if (proxy == null) {
                    YLogger.info(TAG, "Create proxy...");
                    proxy = prepareImplAndCreate(context, config.apiKey);
                    proxy.activate(config);
                }
            }
        }
        return proxy;
    }
}
