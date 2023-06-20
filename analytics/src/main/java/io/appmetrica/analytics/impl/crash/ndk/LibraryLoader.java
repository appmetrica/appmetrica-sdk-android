package io.appmetrica.analytics.impl.crash.ndk;

import androidx.annotation.NonNull;

public class LibraryLoader {

    public void loadLibrary(@NonNull String name) {
        System.loadLibrary(name);
    }

}
