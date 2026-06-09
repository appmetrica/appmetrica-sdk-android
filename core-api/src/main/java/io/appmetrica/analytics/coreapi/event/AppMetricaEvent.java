package io.appmetrica.analytics.coreapi.event;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.event.AppMetricaEventData;

/**
 * Base class for typed custom events sent via
 * {@code AppMetrica.reportEvent(AppMetricaEvent)} or
 * {@code IReporter#reportEvent(AppMetricaEvent)}.
 * <p>
 * Do not subclass this class in application code. Concrete subclasses are provided by
 * other AppMetrica modules and supply the binary payload via {@link #getEventData()}.
 */
public abstract class AppMetricaEvent {

    /**
     * Constructor for {@link AppMetricaEvent}.
     */
    protected AppMetricaEvent() {}

    /**
     * Returns the internal event representation.
     *
     * @return the internal event representation.
     */
    @NonNull
    public abstract AppMetricaEventData getEventData();
}
