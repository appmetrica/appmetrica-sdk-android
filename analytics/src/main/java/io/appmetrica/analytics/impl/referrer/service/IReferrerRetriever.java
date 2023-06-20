package io.appmetrica.analytics.impl.referrer.service;

import androidx.annotation.NonNull;

public interface IReferrerRetriever {

    void retrieveReferrer(@NonNull final ReferrerReceivedListener referrerListener) throws Throwable;
}
