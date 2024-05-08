package io.appmetrica.analytics.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.service.AppMetricaServiceDataReporter;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.logger.internal.YLogger;

public class SelfDiagnosticReporter {

    private static final String TAG = "[SelfDiagnosticReporter]";

    @NonNull
    private final String mApiKey;
    @NonNull
    private final Context mContext;
    @Nullable
    private final CounterConfigurationReporterType mReporterType;
    @NonNull
    private final SelfProcessReporter mSelfProcessReporter;

    public SelfDiagnosticReporter(@NonNull String apiKey,
                                  @NonNull Context context,
                                  @NonNull CounterConfigurationReporterType originalReporterType,
                                  @NonNull SelfProcessReporter selfProcessReporter) {
        mApiKey = apiKey;
        mContext = context;
        switch (originalReporterType) {
            case MAIN:
                mReporterType = CounterConfigurationReporterType.SELF_DIAGNOSTIC_MAIN;
                break;
            case MANUAL:
                mReporterType = CounterConfigurationReporterType.SELF_DIAGNOSTIC_MANUAL;
                break;
            default:
                YLogger.warning(TAG, "cannot create self diagnostic reporter for original reporter of type %s",
                    originalReporterType.getStringValue());
                mReporterType = null;
        }
        mSelfProcessReporter = selfProcessReporter;
    }

    public void reportEvent(@NonNull CounterReport report) {
        if (mReporterType != null) {
            try {
                CounterConfiguration counterConfiguration = new CounterConfiguration(mApiKey);
                counterConfiguration.setReporterType(mReporterType);
                mSelfProcessReporter.reportData(
                        AppMetricaServiceDataReporter.TYPE_CORE,
                        report.toBundle(
                                new ReporterEnvironment(
                                        new ProcessConfiguration(mContext, null),
                                        counterConfiguration,
                                        null
                                ).getConfigBundle()
                        )
                );
            } catch (Throwable ex) {
                YLogger.error(TAG, ex);
            }
        }
    }
}
