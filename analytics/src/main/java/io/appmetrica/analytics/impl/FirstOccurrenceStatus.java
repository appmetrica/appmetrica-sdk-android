package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum FirstOccurrenceStatus {
    UNKNOWN(0),
    FIRST_OCCURRENCE(1),
    NON_FIRST_OCCURENCE(2)
    ;

    public final int mStatusCode;

    FirstOccurrenceStatus(final int statusCode) {
        mStatusCode = statusCode;
    }

    @NonNull
    public static FirstOccurrenceStatus fromStatusCode(@Nullable Integer statusCode) {
        if (statusCode != null) {
            for (FirstOccurrenceStatus firstOccurrenceStatus : FirstOccurrenceStatus.values()) {
                if (firstOccurrenceStatus.mStatusCode == statusCode) {
                    return firstOccurrenceStatus;
                }
            }
        }

        return FirstOccurrenceStatus.UNKNOWN;
    }
}
