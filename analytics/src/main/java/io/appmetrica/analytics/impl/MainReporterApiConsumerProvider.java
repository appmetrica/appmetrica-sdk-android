package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;

public class MainReporterApiConsumerProvider {

    @NonNull
    private final IMainReporter mainReporter;

    public MainReporterApiConsumerProvider(@NonNull IMainReporter mainReporter) {
        this.mainReporter = mainReporter;
    }

    @NonNull
    public IMainReporter getMainReporter() {
        return mainReporter;
    }
}
