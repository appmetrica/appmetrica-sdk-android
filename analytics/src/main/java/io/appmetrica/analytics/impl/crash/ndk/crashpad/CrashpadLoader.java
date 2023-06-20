package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.crash.ndk.LibraryLoader;

public class CrashpadLoader {

    private static final String TAG = "[CrashpadLoader]";

    private enum State {
        BLANK, LOADING_ERROR, LOADED;
    }

    private static final CrashpadLoader INSTANCE = new CrashpadLoader();
    private static final String LIB_NAME = "appmetrica-service-native";

    public static CrashpadLoader getInstance() {
        return INSTANCE;
    }

    @NonNull
    private final LibraryLoader loader;

    private State state = State.BLANK;

    private CrashpadLoader() {
        this(new LibraryLoader());
    }

    @VisibleForTesting
    CrashpadLoader(@NonNull LibraryLoader libraryLoader) {
        loader = libraryLoader;
    }

    public synchronized boolean loadIfNeeded() {
        if (state == State.LOADED) {
            YLogger.debug(TAG, "native library has been already loaded");
            return true;
        } else {
            if (state == State.LOADING_ERROR) {
                YLogger.debug(TAG, "there was unsuccessful attempt to load library");
                return false;
            } else {
                try {
                    loader.loadLibrary(LIB_NAME);
                    state = State.LOADED;
                    YLogger.debug(TAG, "native library is loaded");
                    return true;
                } catch (Throwable e) {
                    YLogger.error(TAG, e, "can't load native library");
                    state = State.LOADING_ERROR;
                    return false;
                }
            }
        }
    }

}
