package io.appmetrica.analytics.impl.component.session;

import androidx.annotation.NonNull;

public enum SessionType {

    FOREGROUND(0), BACKGROUND(1);

    private final int mCode;

    SessionType(int code) {
        this.mCode = code;
    }

    public int getCode() {
        return mCode;
    }

    @NonNull
    public static SessionType getByCode(Integer code) {
        SessionType result = FOREGROUND;
        if (code != null) {
            switch (code) {
                case 0:
                    result = FOREGROUND;
                    break;
                case 1:
                    result = BACKGROUND;
                    break;
            }
        }
        return result;
    }
}
