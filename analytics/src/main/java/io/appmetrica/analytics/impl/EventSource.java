package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;

public enum EventSource {

    NATIVE(0), JS(1), SYSTEM(2);

    public final int code;

    EventSource(int code) {
        this.code = code;
    }

    @NonNull
    public static EventSource fromCode(int code) {
        for (EventSource source : values()) {
            if (source.code == code) {
                return source;
            }
        }
        return NATIVE;
    }
}
