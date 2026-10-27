package io.appmetrica.analytics.impl.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.backport.BiConsumer;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;

public class BackgroundBroadcastReceiver extends BroadcastReceiver {

    @NonNull
    private final BiConsumer<Context, Intent> block;
    @NonNull
    private final ICommonExecutor executor;

    public BackgroundBroadcastReceiver(@NonNull BiConsumer<Context, Intent> block, @NonNull ICommonExecutor executor) {
        this.block = block;
        this.executor = executor;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                block.consume(context, intent);
            }
        });
    }
}
