package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.plugins.IPluginReporter;

public interface IBaseReporter extends IReporterExtended, IPluginReporter {

    boolean isPaused();

    void reportJsEvent(@NonNull String eventName, @Nullable String eventValue);

    void reportJsInitEvent(@NonNull String value);
}
