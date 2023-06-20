package io.appmetrica.analytics.networktasks.internal;

public interface NetworkServiceLifecycleObserver {

    void onCreate();

    void onDestroy();
}
