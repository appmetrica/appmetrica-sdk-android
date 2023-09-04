package io.appmetrica.analytics.identifiers.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;

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
        YLogger.info(tag, "Bind service with intent = %s", intent);
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
                        YLogger.error(tag, e);
                    }
                }
            }
        }
        return service;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        YLogger.info(tag, "onServiceConnected for name = %s; service = %s", name, service);
        synchronized (monitor) {
            this.service = service;
            monitor.notifyAll();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        YLogger.info(tag, "onServiceDisconnected for name = %s", name);
        synchronized (monitor) {
            this.service = null;
            monitor.notifyAll();
        }
    }

    @Override
    public void onBindingDied(ComponentName name) {
        YLogger.info(tag, "onBindingDied for name = %s", name);
        synchronized (monitor) {
            this.service = null;
            monitor.notifyAll();
        }
    }

    @Override
    public void onNullBinding(ComponentName name) {
        YLogger.info(tag, "onNullBinding for name = %s", name);
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
