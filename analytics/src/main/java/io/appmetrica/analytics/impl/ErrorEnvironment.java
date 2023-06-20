package io.appmetrica.analytics.impl;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.utils.MeasuredJsonMap;
import io.appmetrica.analytics.impl.utils.limitation.SimpleMapLimitation;
import org.json.JSONObject;

public class ErrorEnvironment {

    private MeasuredJsonMap mEnvironmentValues;
    private SimpleMapLimitation mLimitation;

    public static final String TAG = "Crash Environment";

    ErrorEnvironment(SimpleMapLimitation limitation) {
        mEnvironmentValues = new MeasuredJsonMap();
        mLimitation = limitation;
    }

    void put(String key, String value) {
        mLimitation.tryToAddValue(mEnvironmentValues, key, value);
    }

    @Nullable
    String toJsonString() {
        return mEnvironmentValues.isEmpty() ? null : new JSONObject(mEnvironmentValues).toString();
    }

    @VisibleForTesting
    public MeasuredJsonMap getEnvironmentValues() {
        return mEnvironmentValues;
    }
}
