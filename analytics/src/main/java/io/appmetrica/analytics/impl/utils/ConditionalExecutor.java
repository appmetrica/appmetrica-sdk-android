package io.appmetrica.analytics.impl.utils;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.NonNullConsumer;
import java.util.ArrayList;
import java.util.List;

public class ConditionalExecutor<T> {

    @Nullable
    private T resource;
    @NonNull
    private final ICommonExecutor executor;
    private final List<NonNullConsumer<T>> commands = new ArrayList<NonNullConsumer<T>>();

    @AnyThread
    public ConditionalExecutor(@NonNull ICommonExecutor executor) {
        this.executor = executor;
    }

    @AnyThread
    public void addCommand(@NonNull final NonNullConsumer<T> command) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (ConditionalExecutor.this) {
                    final T resourceCopy = resource;
                    if (resourceCopy  == null) {
                        commands.add(command);
                    } else {
                        command.consume(resourceCopy);
                    }
                }
            }
        });
    }

    @WorkerThread
    public synchronized void setResource(@NonNull final T value) {
        resource = value;
        for (final NonNullConsumer<T> command : commands) {
            command.consume(value);
        }
        commands.clear();
    }
}
