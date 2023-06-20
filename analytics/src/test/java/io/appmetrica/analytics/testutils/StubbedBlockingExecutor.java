package io.appmetrica.analytics.testutils;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;

public class StubbedBlockingExecutor implements IHandlerExecutor {

    @Override
    public void execute(@NonNull Runnable runnable) {
        runnable.run();
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        FutureTask<T> futureTask = new FutureTask<T>(callable);
        futureTask.run();
        return futureTask;
    }

    @Override
    public void executeDelayed(@NonNull Runnable runnable, long delay) {
        runnable.run();
    }

    @Override
    public void executeDelayed(@NonNull Runnable runnable, long delay, @NonNull TimeUnit timeUnit) {
        runnable.run();
    }

    @Override
    public void remove(@NonNull Runnable runnable) {
        //Do nothing
    }

    @Override
    public void removeAll() {
        // Do nothing
    }

    @NonNull
    @Override
    public Handler getHandler() {
        return TestUtils.createBlockingExecutionHandlerStub();
    }

    @NonNull
    @Override
    public Looper getLooper() {
        return mock(Looper.class);
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public void stopRunning() {
        //Do nothing
    }
}
