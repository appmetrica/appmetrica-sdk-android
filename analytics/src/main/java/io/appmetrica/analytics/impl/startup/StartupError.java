package io.appmetrica.analytics.impl.startup;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum StartupError {

    UNKNOWN (0), NETWORK(1), PARSE(2);

    private static final String BUNDLE_KEY_CODE = "startup_error_key_code";

    private int mCode;

    StartupError(final int code) {
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }

    public Bundle toBundle(Bundle bundle) {
        bundle.putInt(BUNDLE_KEY_CODE, getCode());
        return bundle;
    }

    @Nullable
    public static StartupError fromBundle(Bundle bundle) {
        if (bundle.containsKey(BUNDLE_KEY_CODE)) {
            int code = bundle.getInt(BUNDLE_KEY_CODE);
            return StartupError.fromCode(code);
        } else {
            return null;
        }
    }

    @NonNull
    private static StartupError fromCode(final int code) {
        StartupError error = UNKNOWN;
        switch (code) {
            case 1:
                error = NETWORK;
                break;
            case 2:
                error = PARSE;
                break;
        }
        return error;
    }
}
