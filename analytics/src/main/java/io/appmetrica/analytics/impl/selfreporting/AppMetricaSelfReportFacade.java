package io.appmetrica.analytics.impl.selfreporting;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.SdkData;

public class AppMetricaSelfReportFacade {

    @NonNull
    public static IReporterExtended getReporter() {
        return SelfReportFacadeProvider.getReporterWrapper();
    }

    public static void onInitializationFinished(@NonNull Context context) {
        SelfReportFacadeProvider.getReporterWrapper().onInitializationFinished(context);
    }

    public static void warmupForMetricaProcess(@NonNull Context context) {
        AppMetrica.getReporter(context, SdkData.SDK_API_KEY_UUID);
    }
}
