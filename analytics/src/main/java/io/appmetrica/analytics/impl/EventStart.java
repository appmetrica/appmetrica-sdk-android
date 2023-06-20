package io.appmetrica.analytics.impl;

import androidx.annotation.Nullable;

public class EventStart {

    @Nullable
    public final String buildId;

    public EventStart(@Nullable String buildId) {
        this.buildId = buildId;
    }
}
