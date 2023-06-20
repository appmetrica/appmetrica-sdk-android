package io.appmetrica.analytics.impl.utils.concurrency;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import java.util.concurrent.locks.ReentrantLock;

public class ExclusiveMultiProcessFileLock {

    @NonNull
    private final ReentrantLock reentrantLock;
    @NonNull
    private final FileLocker fileLocker;

    public ExclusiveMultiProcessFileLock(@NonNull Context context, @NonNull String simpleFileName) {
        this(new ReentrantLock(), new FileLocker(context, simpleFileName));
    }

    public void lock() throws Throwable {
        reentrantLock.lock();
        fileLocker.lock();
    }

    public void unlock() {
        fileLocker.unlock();
        reentrantLock.unlock();
    }

    public void unlockAndClear() {
        fileLocker.unlockAndClear();
        reentrantLock.unlock();
    }

    @VisibleForTesting
    ExclusiveMultiProcessFileLock(@NonNull ReentrantLock reentrantLock,
                                  @NonNull FileLocker fileLocker) {
        this.reentrantLock = reentrantLock;
        this.fileLocker = fileLocker;
    }
}
