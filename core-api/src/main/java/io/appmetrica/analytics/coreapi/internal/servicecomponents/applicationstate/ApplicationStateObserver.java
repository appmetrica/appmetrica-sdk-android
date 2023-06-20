package io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate;

import androidx.annotation.NonNull;

public interface ApplicationStateObserver {

    void onApplicationStateChanged(@NonNull ApplicationState applicationState);

}
