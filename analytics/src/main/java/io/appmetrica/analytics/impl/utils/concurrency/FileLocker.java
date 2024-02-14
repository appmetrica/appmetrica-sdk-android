package io.appmetrica.analytics.impl.utils.concurrency;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.io.FileUtils;
import io.appmetrica.analytics.impl.IOUtils;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class FileLocker {

    @NonNull
    private final File lockFile;
    private FileLock mLock;
    private RandomAccessFile mStream;
    private FileChannel mChannel;
    private int lockHoldersCount = 0;

    private static final String TAG = "[DatabaseFileLock]";

    public FileLocker(@NonNull Context context, @NonNull String simpleFileName) {
        this(FileLocker.lockFileFromName(context, simpleFileName));
    }

    public FileLocker(@NonNull String filePath) {
        this(FileUtils.getFileFromPath(filePath + ".lock"));
    }

    FileLocker(@NonNull File lockFile) {
        this.lockFile = lockFile;
    }

    public synchronized void lock() throws Throwable {
        mStream = new RandomAccessFile(lockFile, "rw");
        mChannel = mStream.getChannel();
        if (lockHoldersCount == 0) {
            mLock = mChannel.lock();
        }
        lockHoldersCount++;
    }

    public synchronized void unlock() {
        String path = lockFile.getAbsolutePath();
        lockHoldersCount--;
        if (lockHoldersCount == 0) {
            IOUtils.releaseFileLock(path, mLock);
        }
        Utils.closeCloseable(mStream);
        Utils.closeCloseable(mChannel);
        mStream = null;
        mLock = null;
        mChannel = null;
    }

    public synchronized void unlockAndClear() {
        unlock();
        if (!lockFile.delete()) {
            YLogger.warning(TAG, "Could not unlock lock file for %s", lockFile.getName());
        }
    }

    private static File lockFileFromName(@NonNull Context context, @NonNull String fileName) {
        File result = FileUtils.getFileFromSdkStorage(context, fileName + ".lock");
        if (result == null) {
            throw new IllegalStateException("Cannot create lock file");
        }
        return result;
    }
}
