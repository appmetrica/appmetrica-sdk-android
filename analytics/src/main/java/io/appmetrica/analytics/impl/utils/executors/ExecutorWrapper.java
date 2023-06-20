package io.appmetrica.analytics.impl.utils.executors;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class ExecutorWrapper implements IHandlerExecutor {

    @NonNull
    private final Looper mLooper;
    @NonNull
    private final Handler mHandler;
    @NonNull
    private final InterruptionSafeHandlerThread mHandlerThread;

    public ExecutorWrapper(@NonNull String threadName) {
        this(createLaunchedHandlerThread(threadName));
    }

    @NonNull
    @Override
    public Handler getHandler() {
        return mHandler;
    }

    @NonNull
    @Override
    public Looper getLooper() {
        return mLooper;
    }

    @Override
    public void execute(@NonNull Runnable runnable) {
        mHandler.post(runnable);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        FutureTask<T> futureTask = new FutureTask<T>(callable);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public void executeDelayed(@NonNull Runnable runnable, long delay) {
        executeDelayed(runnable, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void executeDelayed(@NonNull Runnable runnable, long delay, @NonNull TimeUnit timeUnit) {
        mHandler.postDelayed(runnable, timeUnit.toMillis(delay));
    }

    @Override
    public void remove(@NonNull Runnable runnable) {
        mHandler.removeCallbacks(runnable);
    }

    @Override
    public void removeAll() {
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean isRunning() {
        return mHandlerThread.isRunning();
    }

    @Override
    public void stopRunning() {
        mHandlerThread.stopRunning();
    }

    private static InterruptionSafeHandlerThread createLaunchedHandlerThread(@NonNull String threadName) {
        InterruptionSafeHandlerThread handlerThread = new NamedThreadFactory(threadName).newHandlerThread();
        handlerThread.start();
        return handlerThread;
    }

    @VisibleForTesting
    ExecutorWrapper(@NonNull InterruptionSafeHandlerThread handlerThread) {
        this(handlerThread, handlerThread.getLooper(), new Handler(handlerThread.getLooper()));
    }

    @VisibleForTesting
    public ExecutorWrapper(@NonNull InterruptionSafeHandlerThread handlerThread,
                           @NonNull Looper looper,
                           @NonNull Handler handler) {
        mHandlerThread = handlerThread;
        mLooper = looper;
        mHandler = handler;
    }
}
