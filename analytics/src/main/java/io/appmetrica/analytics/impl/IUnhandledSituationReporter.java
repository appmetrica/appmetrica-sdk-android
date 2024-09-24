package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException;

public interface IUnhandledSituationReporter {

    void reportUnhandledException(@NonNull UnhandledException unhandledException);

}
