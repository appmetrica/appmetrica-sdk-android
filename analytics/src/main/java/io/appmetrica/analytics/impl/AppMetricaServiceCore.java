package io.appmetrica.analytics.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;

public interface AppMetricaServiceCore extends AppMetricaServiceLifecycleCallback {

    void reportData(final int type, final Bundle data);

    void resumeUserSession(@NonNull Bundle data);

    void pauseUserSession(@NonNull Bundle data);
}
