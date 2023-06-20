package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

public class MainReporterApiConsumerProvider {

    @NonNull
    private final IMainReporter mainReporter;
    @NonNull
    private final DeeplinkConsumer deeplinkConsumer;

    public MainReporterApiConsumerProvider(@NonNull IMainReporter mainReporter) {
        this(mainReporter, new DeeplinkConsumer(mainReporter));
    }

    @VisibleForTesting
    MainReporterApiConsumerProvider(@NonNull IMainReporter mainReporter,
                                    @NonNull DeeplinkConsumer deeplinkConsumer) {
        this.mainReporter = mainReporter;
        this.deeplinkConsumer = deeplinkConsumer;
    }

    @NonNull
    public IMainReporter getMainReporter() {
        return mainReporter;
    }

    @NonNull
    public DeeplinkConsumer getDeeplinkConsumer() {
        return deeplinkConsumer;
    }
}
