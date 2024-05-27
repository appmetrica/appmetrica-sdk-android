package io.appmetrica.analytics.identifiers.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class AdvIdServiceConnection implements ServiceConnection {

    private static final String TAG_PATTERN = "[AdvServiceConnection-%s]";

    @NonNull
    private final Intent intent;
    @NonNull
    private final String tag;
    @Nullable
    private IBinder service;
    private final Object monitor = new Object();

    public AdvIdServiceConnection(@NonNull Intent intent, @NonNull String serviceShortTag) {
        this.intent = intent;
        this.tag = String.format(TAG_PATTERN, serviceShortTag);
    }

    public boolean bindService(@NonNull Context context) {
        DebugLogger.INSTANCE.info(tag, "Bind service with intent = %s", intent);
        return context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    public void unbindService(@NonNull Context context) {
        synchronized (monitor) {
            this.service = null;
            monitor.notifyAll();
        }
        context.unbindService(this);
    }

    public IBinder awaitBinding(long timeout) {
        if (service == null) {
            synchronized (monitor) {
                if (service == null) {
                    try {
                        monitor.wait(timeout);
                    } catch (InterruptedException e) {
                        DebugLogger.INSTANCE.error(tag, e);
                    }
                }
            }
        }
        return service;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        DebugLogger.INSTANCE.info(tag, "onServiceConnected for name = %s; service = %s", name, service);
        synchronized (monitor) {
            this.service = service;
            monitor.notifyAll();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        DebugLogger.INSTANCE.info(tag, "onServiceDisconnected for name = %s", name);
        synchronized (monitor) {
            this.service = null;
            monitor.notifyAll();
        }
    }

    @Override
    public void onBindingDied(ComponentName name) {
        DebugLogger.INSTANCE.info(tag, "onBindingDied for name = %s", name);
        synchronized (monitor) {
            this.service = null;
            monitor.notifyAll();
        }
    }

    @Override
    public void onNullBinding(ComponentName name) {
        DebugLogger.INSTANCE.info(tag, "onNullBinding for name = %s", name);
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }

    @VisibleForTesting
    @Nullable
    IBinder getBinder() {
        return this.service;
    }

    @NonNull
    public Intent getIntent() {
        return intent;
    }
}
