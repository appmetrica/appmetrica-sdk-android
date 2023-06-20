package io.appmetrica.analytics.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;

/**
 * This class repeats after AIDL IAppMetricaService protocol.
 * When the latter is updated, the same methods should be added here.
 */
public class SelfProcessReporter {

    @NonNull
    private final AppMetricaServiceCore mAppMetricaServiceCore;

    public SelfProcessReporter(@NonNull AppMetricaServiceCore appMetricaServiceCore) {
        mAppMetricaServiceCore = appMetricaServiceCore;
    }

    public void reportData(int type, Bundle data) {
        mAppMetricaServiceCore.reportData(type, data);
    }

    public void resumeUserSession(@NonNull Bundle bundle) {
        mAppMetricaServiceCore.resumeUserSession(bundle);
    }

    public void pauseUserSession(@NonNull Bundle bundle) {
        mAppMetricaServiceCore.pauseUserSession(bundle);
    }
}
