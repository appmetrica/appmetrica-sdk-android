package io.appmetrica.analytics.coreapi.internal.executors;

import androidx.annotation.NonNull;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface ICommonExecutor extends IInterruptionSafeThread, Executor {

    /**
     * Executes a task, possibly inline according to the implementation's execution context.
     * Inline execution can reenter the caller, bypass pending tasks, and propagate task exceptions.
     * Use {@link #post(Runnable)} when the task must be queued.
     *
     * @param runnable the task to execute
     */
    void execute(@NonNull Runnable runnable);

    /**
     * Adds a task to this executor's queue without running it inline.
     */
    void post(@NonNull Runnable runnable);

    <T> Future<T> submit(Callable<T> task);

    /**
     * Adds a delayed task to this executor's queue without running it inline, even if the delay is zero.
     *
     * @param runnable the task to enqueue
     * @param delay the delay in the specified time unit
     * @param timeUnit the unit of the delay
     */
    void postDelayed(@NonNull Runnable runnable, long delay, @NonNull TimeUnit timeUnit);

    void remove(@NonNull Runnable runnable);

    void removeAll();
}
