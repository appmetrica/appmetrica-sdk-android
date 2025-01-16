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
    void reportEvent(@NonNull final ModuleEvent moduleEvent);

    /**
     * Sets session extra for current reporter.
     * This data is stored on disk and used for every event even after application stop.
     *
     * @param key {@link String} key
     * @param value Value of extra
     */
    void setSessionExtra(@NonNull String key, @Nullable byte[] value);

    /**
     * Reports auto collected adRevenue to AppMetrica
     *
     * @param adRevenue AdRevenue
     * @param autoCollected true if data is auto collected and false otherwise
     */
    void reportAdRevenue(@NonNull final AdRevenue adRevenue, final boolean autoCollected);

    /**
     * Initiates forced sending of all stored events from the buffer.<p>
     * AppMetrica SDK doesn't send events immediately after they occurred. It stores events data in the buffer.
     * This method forcibly initiates sending all the data from the buffer and flushes it.<p>
     * Use the method after important checkpoints of user scenarios.
     *
     * <p> <b>WARNING:</b> Frequent use of the method can lead to increasing outgoing internet traffic and
     * energy consumption.
     */
    void sendEventsBuffer();
}
