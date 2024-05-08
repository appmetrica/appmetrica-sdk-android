package io.appmetrica.analytics.impl.crash;

import android.os.FileObserver;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.impl.FileProvider;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.io.File;

public class CrashFileObserver extends FileObserver {

    private static final String TAG = "[CrashFileObserver]";

    private final Consumer<File> newCrashListener;
    private final File crashDirectory;
    @NonNull
    private final FileProvider fileProvider;

    public CrashFileObserver(@NonNull File crashDirectory, @NonNull Consumer<File> newCrashListener) {
        this(crashDirectory, newCrashListener, new FileProvider());
    }

    @VisibleForTesting
    CrashFileObserver(@NonNull File crashDirectory,
                      @NonNull Consumer<File> newCrashListener,
                      @NonNull FileProvider fileProvider) {
        super(crashDirectory.getAbsolutePath(), FileObserver.ALL_EVENTS);
        YLogger.debug(
            TAG,
            "start watching directory %s for events with type %d",
            crashDirectory.getAbsolutePath(),
            FileObserver.CLOSE_WRITE
        );
        this.newCrashListener = newCrashListener;
        this.crashDirectory = crashDirectory;
        this.fileProvider = fileProvider;
    }

    @Override
    public void onEvent(int event, @Nullable String path) {
        YLogger.debug(TAG, "event %d for path %s received", event, path);
        if (event == FileObserver.CLOSE_WRITE && TextUtils.isEmpty(path) == false) {
            newCrashListener.consume(fileProvider.getFileByNonNullPath(crashDirectory, path));
            YLogger.debug(TAG, "event %d for path %s handled", event, path);
        }
    }
}
