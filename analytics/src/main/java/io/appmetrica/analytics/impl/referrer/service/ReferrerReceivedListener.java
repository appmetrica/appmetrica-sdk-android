package io.appmetrica.analytics.impl.referrer.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;

public interface ReferrerReceivedListener {

    @WorkerThread
    void onReferrerReceived(@Nullable ReferrerInfo info);

    @WorkerThread
    void onReferrerRetrieveError(@NonNull Throwable exception);

}
