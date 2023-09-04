package io.appmetrica.analytics.impl;

import android.content.Intent;
import android.content.res.Configuration;

public interface AppMetricaServiceLifecycleCallback {

    void onCreate();

    void onStart(final Intent intent, final int startId);

    void onStartCommand(final Intent intent, final int flags, final int startId);

    void onBind(final Intent intent);

    void onRebind(final Intent intent);

    void onUnbind(final Intent intent);

    void onDestroy();

    void onConfigurationChanged(Configuration newConfig);

}
