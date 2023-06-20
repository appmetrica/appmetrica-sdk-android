package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;

public interface IReporterFactoryProvider {

    @NonNull
    IReporterFactory getReporterFactory();
}
