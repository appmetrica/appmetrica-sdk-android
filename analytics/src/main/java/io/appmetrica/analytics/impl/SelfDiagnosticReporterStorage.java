package io.appmetrica.analytics.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.internal.CounterConfigurationReporterType;

import java.util.HashMap;
import java.util.Map;

public class SelfDiagnosticReporterStorage {

    @NonNull
    private final SelfProcessReporter mSelfProcessReporter;
    @NonNull
    private final Context mContext;

    @NonNull
    private final Map<String, SelfDiagnosticReporter> mReporters;

    public SelfDiagnosticReporterStorage(@NonNull Context context, @NonNull SelfProcessReporter selfProcessReporter) {
        mContext = context;
        mSelfProcessReporter = selfProcessReporter;
        mReporters = new HashMap<String, SelfDiagnosticReporter>();
    }

    @NonNull
    public synchronized SelfDiagnosticReporter getOrCreateReporter(
            @NonNull String apiKey,
            @NonNull CounterConfigurationReporterType reporterType
    ) {
        SelfDiagnosticReporter reporter = mReporters.get(apiKey);
        if (reporter == null) {
            reporter = new SelfDiagnosticReporter(
                    apiKey,
                    mContext,
                    reporterType,
                    mSelfProcessReporter
            );
            mReporters.put(apiKey, reporter);
        }
        return reporter;
    }
}
