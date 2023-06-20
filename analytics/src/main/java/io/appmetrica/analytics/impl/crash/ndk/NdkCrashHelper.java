package io.appmetrica.analytics.impl.crash.ndk;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.FileProvider;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.crash.ndk.crashpad.CrashpadInitializer;
import java.util.Arrays;
import java.util.List;

public class NdkCrashHelper {

    private static final String TAG = "[NdkCrashHelper]";

    @Nullable
    private String nativeCrashFolder;

    private final Context context;
    private final List<NdkCrashInitializer> initializers;
    private final LibraryLoader libraryLoader;

    private boolean crashesHandlingEnabled;
    private boolean initialized;
    @Nullable
    private NdkCrashInitializer currentInitializer;

    @NonNull
    private final FileProvider fileProvider;

    @SuppressLint("NewApi")
    public NdkCrashHelper(@NonNull Context context,
                          @NonNull ProcessConfiguration processConfiguration
    ) {
        this(
            context,
            Arrays.asList(
                (NdkCrashInitializer) new CrashpadInitializer(context, processConfiguration)
            ),
            new FileProvider(),
            new LibraryLoader()
        );
    }

    @VisibleForTesting
    NdkCrashHelper(
            @NonNull Context context,
            @NonNull List<NdkCrashInitializer> crashInitializer,
            @NonNull FileProvider fileProvider,
            @NonNull LibraryLoader loader
    ) {
        this.context = context;
        this.initializers = crashInitializer;
        this.fileProvider = fileProvider;
        this.libraryLoader = loader;
    }

    @WorkerThread
    public synchronized void setReportsEnabled(final boolean enabled,
                                               @NonNull String apiKey,
                                               @Nullable String errorEnv
    ) {
        YLogger.debug(TAG, "set reports enabled %b", enabled);
        if (enabled) {
            setUpNativeCrashesHandler(apiKey, errorEnv);
        } else cancelSetUpNativeCrashesHandler();
    }

    private void initNativePartIfNeeded() {
        if (initialized == false) {
            currentInitializer = loadNativeCrashesLibrary();
            if (currentInitializer != null) {
                setNativeLogsEnabled(YLogger.DEBUG);
                nativeCrashFolder = fileProvider.getStorageSubDirectory(context, currentInitializer.getFolderName());
            }
        }
        initialized = true;
    }

    @WorkerThread
    private void setUpNativeCrashesHandler(@NonNull String apiKey, @Nullable String errorEnv) {
        try {
            initNativePartIfNeeded();
            YLogger.debug(TAG, "crashesHandlingEnabled=%b", crashesHandlingEnabled);
            if (isLibraryLoaded() && nativeCrashFolder != null && !crashesHandlingEnabled) {
                currentInitializer.setUpHandler(apiKey, nativeCrashFolder, errorEnv);
                crashesHandlingEnabled = true;
            }
        } catch (Throwable error) {
            crashesHandlingEnabled = false;
            YLogger.error(TAG, error, "Unsuccessful attempt to setup native uncaught exception handler");
        }
    }

    private synchronized boolean isLibraryLoaded() {
        return currentInitializer != null;
    }

    private synchronized void cancelSetUpNativeCrashesHandler() {
        try {
            if (isLibraryLoaded() && crashesHandlingEnabled) {
                currentInitializer.cancelSetUp();
            }
        } catch (Throwable error) {
            YLogger.error(TAG, error, "Unsuccessful attempt to cancel setup native crashes handler");
        }
        crashesHandlingEnabled = false;
    }

    private void setNativeLogsEnabled(final boolean enabled) {
        try {
            currentInitializer.setLogsEnabled(enabled);
        } catch (Throwable error) {
            YLogger.error(TAG, error, "Unsuccessful attempt to enable logs");
        }
    }

    @VisibleForTesting
    NdkCrashInitializer loadNativeCrashesLibrary() {
        for (NdkCrashInitializer initializer: initializers) {
            try {
                libraryLoader.loadLibrary(initializer.getLibraryName());
                YLogger.debug(TAG, "Library %s loaded", initializer.getLibraryName());
                return initializer;
            } catch (Throwable error) {
                YLogger.debug(TAG, "Library %s not found", initializer.getLibraryName());
            }
        }
        YLogger.error(TAG, "Native crashes are disabled, no library");
        return null;
    }

    public void updateErrorEnvironment(@NonNull String errorEnvironment) {
        if (currentInitializer != null) {
            currentInitializer.updateErrorEnvironment(errorEnvironment);
        }
    }
}
