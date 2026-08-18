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

    public enum BindingState {
        WAITING,
        CONNECTED,
        NULL_BINDING,
        DISCONNECTED,
        BINDING_DIED,
        INTERRUPTED,
        TIMED_OUT
    }

    private static final String TAG_PATTERN = "[AdvServiceConnection-%s]";

    @NonNull
    private final Intent intent;
    @NonNull
    private final String tag;
    @Nullable
    private IBinder service;
    private final Object monitor = new Object();
    @NonNull
    private BindingState bindingState = BindingState.WAITING;

    public AdvIdServiceConnection(@NonNull Intent intent, @NonNull String serviceShortTag) {
        this.intent = intent;
        this.tag = String.format(TAG_PATTERN, serviceShortTag);
    }

    public boolean bindService(@NonNull Context context) {
        DebugLogger.INSTANCE.info(tag, "Bind service with intent = %s", intent);
        synchronized (monitor) {
            service = null;
            bindingState = BindingState.WAITING;
        }
        return context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    public void unbindService(@NonNull Context context) {
        synchronized (monitor) {
            this.service = null;
            bindingState = BindingState.DISCONNECTED;
            monitor.notifyAll();
        }
        context.unbindService(this);
    }

    public IBinder awaitBinding(long timeout) {
        synchronized (monitor) {
            if (service == null && bindingState == BindingState.WAITING) {
                try {
                    monitor.wait(timeout);
                } catch (InterruptedException exception) {
                    bindingState = BindingState.INTERRUPTED;
                    Thread.currentThread().interrupt();
                    DebugLogger.INSTANCE.error(tag, exception);
                }
            }
            if (service == null && bindingState == BindingState.WAITING) {
                bindingState = BindingState.TIMED_OUT;
            }
            return service;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        DebugLogger.INSTANCE.info(tag, "onServiceConnected for name = %s; service = %s", name, service);
        synchronized (monitor) {
            this.service = service;
            bindingState = BindingState.CONNECTED;
            monitor.notifyAll();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        DebugLogger.INSTANCE.info(tag, "onServiceDisconnected for name = %s", name);
        synchronized (monitor) {
            this.service = null;
            bindingState = BindingState.DISCONNECTED;
            monitor.notifyAll();
        }
    }

    @Override
    public void onBindingDied(ComponentName name) {
        DebugLogger.INSTANCE.info(tag, "onBindingDied for name = %s", name);
        synchronized (monitor) {
            this.service = null;
            bindingState = BindingState.BINDING_DIED;
            monitor.notifyAll();
        }
    }

    @Override
    public void onNullBinding(ComponentName name) {
        DebugLogger.INSTANCE.info(tag, "onNullBinding for name = %s", name);
        synchronized (monitor) {
            bindingState = BindingState.NULL_BINDING;
            monitor.notifyAll();
        }
    }

    @VisibleForTesting
    @Nullable
    IBinder getBinder() {
        return this.service;
    }

    @NonNull
    BindingState getBindingState() {
        synchronized (monitor) {
            return bindingState;
        }
    }

    @NonNull
    public Intent getIntent() {
        return intent;
    }
}
