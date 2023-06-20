package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Handler;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.referrer.client.ReferrerHelper;
import io.appmetrica.analytics.impl.startup.StartupHelper;

class AppMetricaImplFieldsProvider {

    @NonNull
    DataResultReceiver createDataResultReceiver(@NonNull Handler handler, @NonNull AppMetricaImpl appMetrica) {
        return new DataResultReceiver(handler, appMetrica);
    }

    @NonNull
    ProcessConfiguration createProcessConfiguration(@NonNull Context context,
                                                    @NonNull DataResultReceiver dataResultReceiver) {
        return new ProcessConfiguration(context, dataResultReceiver);
    }

    @NonNull
    ReportsHandler createReportsHandler(@NonNull ProcessConfiguration processConfiguration,
                                        @NonNull Context context,
                                        @NonNull ICommonExecutor executor) {
        return new ReportsHandler(processConfiguration, context, executor);
    }

    @NonNull
    StartupHelper createStartupHelper(@NonNull Context context,
                                      @NonNull ReportsHandler reportsHandler,
                                      @NonNull PreferencesClientDbStorage clientPreferences,
                                      @NonNull Handler handler) {
        return new StartupHelper(context, reportsHandler, clientPreferences, handler);
    }

    @NonNull
    ReferrerHelper createReferrerHelper(@NonNull ReportsHandler reportsHandler,
                                        @NonNull PreferencesClientDbStorage clientPreferences,
                                        @NonNull Handler handler) {
        return new ReferrerHelper(reportsHandler, clientPreferences, handler);
    }

    @NonNull
    ReporterFactory createReporterFactory(@NonNull Context context,
                                          @NonNull ProcessConfiguration processConfiguration,
                                          @NonNull ReportsHandler reportsHandler,
                                          @NonNull Handler handler,
                                          @NonNull StartupHelper startupHelper) {
        return new ReporterFactory(context, processConfiguration, reportsHandler, handler, startupHelper);
    }
}
