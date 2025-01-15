package io.appmetrica.analytics.impl.selfreporting;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.SdkData;
import io.appmetrica.analytics.impl.proxy.AppMetricaProxyProvider;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.concurrent.TimeUnit;

public class AppMetricaSelfReportFacade {

    private static final String TAG = "[AppMetricaSelfReportFacade]";

    private static final long SELF_REPORTER_INITIALIZATION_DELAY_SECONDS = 5L;

    @NonNull
    public static IReporterExtended getReporter() {
        return SelfReportFacadeProvider.getReporterWrapper();
    }

    public static void onInitializationFinished(@NonNull Context context) {
        DebugLogger.INSTANCE.info(
            TAG,
            "onInitializationFinished. Schedule self reporter initialization in %s seconds",
            SELF_REPORTER_INITIALIZATION_DELAY_SECONDS
        );
        ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor().executeDelayed(
            new Runnable() {
                @Override
                public void run() {
                    SelfReportFacadeProvider.getReporterWrapper().onInitializationFinished(context);
                }
            },
            TimeUnit.SECONDS.toMillis(SELF_REPORTER_INITIALIZATION_DELAY_SECONDS)
        );
    }

    public static void onFullyInitializationFinished(@NonNull Context context) {
        DebugLogger.INSTANCE.info(
            TAG,
            "onFullyInitializationFinished. Schedule Self reporter initialization immediately"
        );
        ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor().execute(
            new Runnable() {
                @Override
                public void run() {
                    SelfReportFacadeProvider.getReporterWrapper().onInitializationFinished(context);
                }
            }
        );
    }

    public static void warmupForSelfProcess(@NonNull Context context) {
        DebugLogger.INSTANCE.info(
            TAG,
            "Warm up for self process"
        );
        AppMetricaProxyProvider.getProxy().warmUpForSelfProcess(context);
        AppMetrica.getReporter(context, SdkData.SDK_API_KEY_UUID);
    }
}
