package io.appmetrica.analytics.impl;

public interface ServiceLifecycleObserver {

    void onCreate();

    void onDestroy();
}
