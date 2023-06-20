package io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum ApplicationState {
    UNKNOWN("unknown"),
    BACKGROUND("background"),
    VISIBLE("visible");

    private final String mStringValue;

    ApplicationState(String stringValue) {
        mStringValue = stringValue;
    }

    @NonNull
    public String getStringValue() {
        return mStringValue;
    }

    @NonNull
    public static ApplicationState fromString(@Nullable String value) {
        ApplicationState applicationState = ApplicationState.UNKNOWN;
        for (ApplicationState state : ApplicationState.values()) {
            if (state.mStringValue.equals(value)) {
                applicationState = state;
                break;
            }
        }
        return applicationState;
    }
}
