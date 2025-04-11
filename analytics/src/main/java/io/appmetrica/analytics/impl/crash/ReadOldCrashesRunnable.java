package io.appmetrica.analytics.impl.crash;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.io.File;

public class ReadOldCrashesRunnable implements Runnable {

    private static final String TAG = "[ReadOldCrashesRunnable]";

    @NonNull
    private final File crashDirectory;
    @NonNull
    private final Consumer<File> crashConsumer;

    public ReadOldCrashesRunnable(@NonNull Context context,
                                  @NonNull File crashDirectory,
                                  @NonNull Consumer<File> crashConsumer) {
        this(crashDirectory, crashConsumer);
    }

    @VisibleForTesting
    ReadOldCrashesRunnable(@NonNull File crashDirectory,
                           @NonNull Consumer<File> crashConsumer) {
        this.crashDirectory = crashDirectory;
        this.crashConsumer = crashConsumer;
    }

    @Override
    public void run() {
        DebugLogger.INSTANCE.info(TAG, "read crashes from directory %s", crashDirectory.getAbsolutePath());
        if (crashDirectory.exists() && crashDirectory.isDirectory()) {
            File[] files = crashDirectory.listFiles();
            if (files != null && files.length != 0) {
                for (File file : files) {
                    try {
                        DebugLogger.INSTANCE.info(TAG, "handle file %s", file.getName());
                        crashConsumer.consume(file);
                    } catch (Throwable ex) {
                        DebugLogger.INSTANCE.error(TAG, ex);
                    }
                }
            } else {
                DebugLogger.INSTANCE.info(TAG, "there is no files in %s directory", crashDirectory.getName());
            }
        } else {
            DebugLogger.INSTANCE.info(
                TAG,
                "directory %s exists %b, isDirectory %b",
                crashDirectory.getName(),
                crashDirectory.exists(),
                crashDirectory.isDirectory()
            );
        }
    }
}
