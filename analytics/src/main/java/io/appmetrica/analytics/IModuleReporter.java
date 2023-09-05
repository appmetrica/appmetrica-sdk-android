package io.appmetrica.analytics;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Analogue of {@link IReporter} for custom modules and plugins.
 * {@link IModuleReporter} can send events to an alternative api key, differ from
 * api key, passed to {@link AppMetrica#activate(Context, AppMetricaConfig)}
 * Instance of object can be obtained via
 * {@link ModulesFacade#getModuleReporter(Context, String)} method call.
 * For every api key only one {@link IModuleReporter} instance is created.
 * You can either query it each time you need it, or save the reference by yourself.
 */
public interface IModuleReporter {

    /**
     * Sends report by event name.
     *
     * @param moduleEvent Event parameters
     */
    void reportEvent(
        @NonNull final ModuleEvent moduleEvent
    );

    /**
     * Sets session extra for current reporter.
     * This data is stored on disk and used for every event even after application stop.
     *
     * @param key {@link String} key
     * @param value Value of extra
     */
    void setSessionExtra(@NonNull String key, @Nullable byte[] value);
}
