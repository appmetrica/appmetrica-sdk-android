package io.appmetrica.analytics;

import androidx.annotation.WorkerThread;

/**
 * Interface for custom ANR events listener.
 */
public interface AnrListener {

    /**
     * The method that is called if AppMetrica SDK detects that an ANR has occurred.
     */
    @WorkerThread
    void onAppNotResponding();
}
