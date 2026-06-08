package io.appmetrica.analytics.coreapi.event;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.event.AppMetricaEventData;

/**
 * Base class for typed custom events sent via
 * {@link io.appmetrica.analytics.AppMetrica#reportEvent(AppMetricaEvent)} or
 * {@link io.appmetrica.analytics.IReporter#reportEvent(AppMetricaEvent)}.
 * Feature modules subclass this and provide the binary payload via {@link #getEventData()}.
 */
public abstract class AppMetricaEvent {

    /**
     * Returns the internal event representation with type, name and binary payload.
     */
    @NonNull
    public abstract AppMetricaEventData getEventData();
}
