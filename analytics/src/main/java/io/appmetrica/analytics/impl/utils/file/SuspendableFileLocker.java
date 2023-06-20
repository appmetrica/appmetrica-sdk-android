package io.appmetrica.analytics.impl.utils.file;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.FileProvider;
import io.appmetrica.analytics.impl.IOUtils;
import io.appmetrica.analytics.impl.Utils;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public final class SuspendableFileLocker {

    private static final HashMap<String, SuspendableFileLocker> sLocks = new HashMap<String, SuspendableFileLocker>();

    public synchronized static SuspendableFileLocker getLock(
            @NonNull Context context, @NonNull String simpleFileName
    ) {
        SuspendableFileLocker locker = sLocks.get(simpleFileName);
        if (locker == null) {
            locker = new SuspendableFileLocker(context, simpleFileName);
            sLocks.put(simpleFileName, locker);
        }
        return locker;
    }

    @NonNull
    private final String mLockFileName;

    private FileLock lock;
    private FileChannel channel;
    @Nullable
    private final File lockFile;
    private RandomAccessFile stream;
    private Semaphore semaphore = new Semaphore(1, true);

    private SuspendableFileLocker(@NonNull Context context, @NonNull String simpleFileName) {
        mLockFileName = simpleFileName + ".lock";
        FileProvider fileProvider = new FileProvider();
        File lockDir = fileProvider.getFileByPath(context.getCacheDir(), "appmetrica_locks");
        if (lockDir != null) {
            lockDir.mkdirs();
        }
        lockFile = fileProvider.getFileByPath(lockDir, mLockFileName);
    }

    public synchronized void lock() throws Throwable {
        semaphore.acquire();
        if (lockFile == null) {
            throw new IllegalStateException("Lock file is null");
        }
        if (channel == null) {
            stream = new RandomAccessFile(lockFile, "rw");
            channel = stream.getChannel();
        }
        lock = channel.lock();
    }

    public synchronized void unlock() {
        semaphore.release();
        if (semaphore.availablePermits() > 0) {
            IOUtils.releaseFileLock(mLockFileName, lock);
            Utils.closeCloseable(channel);
            Utils.closeCloseable(stream);
            channel = null;
            stream = null;
        }
    }
}
