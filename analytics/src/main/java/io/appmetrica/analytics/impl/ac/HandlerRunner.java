package io.appmetrica.analytics.impl.ac;

import android.annotation.SuppressLint;
import android.os.Build;
import androidx.annotation.NonNull;
import dalvik.system.PathClassLoader;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;

public class HandlerRunner {

    private static final String TAG = "[HandlerRunner]";

    public static void main(String[] args) {
        final String trampolineLibrary = "appmetrica-native-runner";
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.N)) {
            loadLibraryViaSystemClassloader(trampolineLibrary);
        } else {
            loadViaFallbackClassloader(trampolineLibrary);
        }
        runHandler(args);
        YLogger.debug(TAG, "run java main");
    }

    private static void loadLibraryViaSystemClassloader(@NonNull String name) {
        try {
            System.loadLibrary(name);
        } catch (Throwable t) {
            YLogger.error(TAG, t, "can't load library %s", name);
            loadViaFallbackClassloader(name);
        }
    }

    @SuppressLint("UnsafeDynamicallyLoadedCode")
    private static void loadViaFallbackClassloader(@NonNull String name) {
        try {
            // There is a quirk in the implementation of the system classloader in Android M.
            // The caller of this process (see crashpad_android.cc) sets the CLASSPATH and
            // LD_LIBRARY_PATH to the list of APKs and the list of native libs dirs + list of native
            // libs dirs within the APK. These env vars are then used to initialize system properties
            // java.class.path and java.library.path respectively. These properties should be used to
            // set up system classloader. However, Android M ignores non-directory entries in
            // java.library.path (see https://nda.ya.ru/t/_Hy9h-eJ3iTW3H).

            // To work around this we set up our own classloader passing libraryPath in such a way that
            // APK entries aren't ignored. The we use the classloader to locate the library.
            String classpath = System.getProperty("java.class.path");
            String nativeLibsPath = System.getProperty("java.library.path");
            PathClassLoader fallbackClassLoader = new PathClassLoader(classpath, nativeLibsPath,
                    ClassLoader.getSystemClassLoader());
            String libraryPath = fallbackClassLoader.findLibrary(name);
            if (libraryPath != null) {
                System.load(libraryPath);
            }
        } catch (Throwable t) {
            YLogger.error(TAG, t, "can't load library via fallback %s", name);
        }
    }

    private static native void runHandler(String[] args);

}
