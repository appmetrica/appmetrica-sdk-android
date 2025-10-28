package io.appmetrica.analytics.impl;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class ContextReceiverSafeWrapper {

    private static final String TAG = "[ContextReceiverSafeWrapper]";

    public static class Provider {

        @NonNull
        public ContextReceiverSafeWrapper create(@Nullable BroadcastReceiver receiver) {
            return new ContextReceiverSafeWrapper(receiver);
        }
    }

    @Nullable
    private final BroadcastReceiver receiver;
    private boolean receiverRegistered = false;

    public ContextReceiverSafeWrapper(@Nullable BroadcastReceiver receiver) {
        this.receiver = receiver;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Nullable
    public synchronized Intent registerReceiver(@NonNull Context context,
                                                @NonNull IntentFilter filter,
                                                @NonNull IHandlerExecutor executor) {
        Intent result = null;
        try {
            result = context.registerReceiver(receiver, filter, null, executor.getHandler());
            receiverRegistered = true;
        } catch (Throwable ex) {
            DebugLogger.INSTANCE.error(TAG, ex);
        }
        return result;
    }

    public synchronized void unregisterReceiver(@NonNull Context context) {
        if (receiverRegistered) {
            try {
                context.unregisterReceiver(receiver);
                receiverRegistered = false;
            } catch (Throwable ex) {
                DebugLogger.INSTANCE.error(TAG, ex);
            }
        }
    }
}
