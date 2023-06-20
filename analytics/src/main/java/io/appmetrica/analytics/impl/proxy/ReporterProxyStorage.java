package io.appmetrica.analytics.impl.proxy;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import java.util.HashMap;
import java.util.Map;

public class ReporterProxyStorage {

    private static class Holder {
        private static final ReporterProxyStorage sInstance = new ReporterProxyStorage(
                ClientServiceLocator.getInstance().getApiProxyExecutor(),
                new AppMetricaFacadeProvider()
        );
    }

    private final Map<String, ReporterExtendedProxy> mReportersProxies =
            new HashMap<String, ReporterExtendedProxy>();
    @NonNull
    private final AppMetricaFacadeProvider mFacadeProvider;
    @NonNull
    private final ICommonExecutor mExecutor;

    @NonNull
    public static ReporterProxyStorage getInstance() {
        return Holder.sInstance;
    }

    @VisibleForTesting
    ReporterProxyStorage(@NonNull ICommonExecutor executor, @NonNull AppMetricaFacadeProvider provider) {
        mExecutor = executor;
        mFacadeProvider = provider;
    }

    @NonNull
    private ReporterExtendedProxy prepareImplAndCreate(@NonNull final Context context, @NonNull String apiKey) {
        //workaround for Ad's SDK workaround
        if (mFacadeProvider.peekInitializedImpl() == null) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    mFacadeProvider.getInitializedImpl(context);
                }
            });
        }
        ReporterExtendedProxy proxy = new ReporterExtendedProxy(mExecutor, context, apiKey);
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
        ReporterExtendedProxy proxy = mReportersProxies.get(config.apiKey);
        if (proxy == null) {
            synchronized (mReportersProxies) {
                proxy = mReportersProxies.get(config.apiKey);
                if (proxy == null) {
                    proxy = prepareImplAndCreate(context, config.apiKey);
                    proxy.activate(config);
                }
            }
        }
        return proxy;
    }
}
