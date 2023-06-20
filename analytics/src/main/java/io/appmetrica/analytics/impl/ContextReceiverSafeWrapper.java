package io.appmetrica.analytics.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;

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

    @Nullable
    public synchronized Intent registerReceiver(@NonNull Context context,
                                                @NonNull final IntentFilter filter) {
        Intent result = null;
        try {
            result = context.registerReceiver(receiver, filter);
            receiverRegistered = true;
        } catch (Throwable ex) {
            YLogger.error(TAG, ex);
        }
        return result;
    }

    public synchronized void unregisterReceiver(@NonNull Context context) {
        if (receiverRegistered) {
            try {
                context.unregisterReceiver(receiver);
                receiverRegistered = false;
            } catch (Throwable ex) {
                YLogger.error(TAG, ex);
            }
        }
    }
}
