package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.crash.client.AllThreads;
import io.appmetrica.analytics.impl.crash.client.UnhandledException;

public interface IUnhandledSituationReporter {

    void reportUnhandledException(@NonNull UnhandledException unhandledException);

    void reportAnr(@NonNull AllThreads allThreads);
}
