package io.appmetrica.analytics;

import androidx.annotation.WorkerThread;

public interface AnrListener {
    @WorkerThread
    void onAppNotResponding();
}
