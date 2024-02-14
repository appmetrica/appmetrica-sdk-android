package io.appmetrica.analytics.impl.crash;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.impl.utils.concurrency.ExclusiveMultiProcessFileLock;
import io.appmetrica.analytics.impl.utils.concurrency.FileLocksHolder;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.io.File;

public class ReadOldCrashesRunnable implements Runnable {

    private static final String TAG = "[ReadOldCrashesRunnable]";

    @NonNull
    private final File crashDirectory;
    @NonNull
    private final Consumer<File> newCrashListener;
    @NonNull
    private final FileLocksHolder fileLocksHolder;

    public ReadOldCrashesRunnable(@NonNull Context context,
                                  @NonNull File crashDirectory,
                                  @NonNull Consumer<File> newCrashListener) {
        this(crashDirectory, newCrashListener, FileLocksHolder.getInstance(context));
    }

    @VisibleForTesting
    ReadOldCrashesRunnable(@NonNull File crashDirectory,
                           @NonNull Consumer<File> newCrashListener,
                           @NonNull FileLocksHolder fileLocksHolder) {
        this.crashDirectory = crashDirectory;
        this.newCrashListener = newCrashListener;
        this.fileLocksHolder = fileLocksHolder;
    }

    @Override
    public void run() {
        YLogger.d("%s read crashes from directory %s", TAG, crashDirectory.getAbsolutePath());
        if (crashDirectory.exists() && crashDirectory.isDirectory()) {
            File[] files = crashDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    final ExclusiveMultiProcessFileLock fileLocker = fileLocksHolder.getOrCreate(file.getName());
                    try {
                        fileLocker.lock();
                        YLogger.d("%s handle file %s", TAG, file.getName());
                        newCrashListener.consume(file);
                    } catch (Throwable ex) {
                        YLogger.error(TAG, ex);
                    } finally {
                        fileLocker.unlockAndClear();
                    }
                }
            } else {
                YLogger.d("%s there is no files in %s directory", TAG, crashDirectory.getName());
            }
        } else {
            YLogger.d(
                    "%s directory %s exists %b, isDirectory %b", TAG,
                    crashDirectory.getName(), crashDirectory.exists(), crashDirectory.isDirectory()
            );
        }
    }
}
