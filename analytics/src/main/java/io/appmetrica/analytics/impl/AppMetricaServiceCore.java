package io.appmetrica.analytics.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.service.AppMetricaServiceCallback;

public interface AppMetricaServiceCore extends AppMetricaServiceLifecycleCallback {

    void reportData(final int type, final Bundle data);

    void resumeUserSession(@NonNull Bundle data);

    void pauseUserSession(@NonNull Bundle data);

    void updateCallback(@NonNull AppMetricaServiceCallback callback);
}
