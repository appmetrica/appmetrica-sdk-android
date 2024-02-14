package io.appmetrica.analytics.impl.crash.jvm;

import android.os.FileObserver;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.impl.crash.CrashFileObserver;
import io.appmetrica.analytics.impl.crash.CrashFolderPreparer;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.io.File;

public class CrashDirectoryWatcher {

    private static final String TAG = "[CrashDirectoryWatcher]";

    @NonNull
    private final FileObserver observer;
    @NonNull
    private final File crashDirectory;
    @NonNull
    private final CrashFolderPreparer crashFolderPreparer;

    public CrashDirectoryWatcher(@NonNull File crashDirectory,
                                 @NonNull Consumer<File> newCrashListener) {
        this(
                new CrashFileObserver(crashDirectory, newCrashListener),
                crashDirectory,
                new CrashFolderPreparer()
        );
    }

    @VisibleForTesting()
    CrashDirectoryWatcher(@NonNull FileObserver observer,
                          @NonNull File crashDirectory,
                          @NonNull CrashFolderPreparer crashFolderPreparer) {
        this.observer = observer;
        this.crashDirectory = crashDirectory;
        this.crashFolderPreparer = crashFolderPreparer;
    }

    public void startWatching() {
        crashFolderPreparer.prepareCrashFolder(crashDirectory);
        YLogger.d("%s startWatching for crashDirectory %s", TAG, crashDirectory.getAbsolutePath());
        observer.startWatching();
    }

    public void stopWatching() {
        YLogger.d("%s stopWatching for crashDirectory %s", TAG, crashDirectory.getAbsolutePath());
        observer.stopWatching();
    }
}
