package io.appmetrica.analytics.impl.crash;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.impl.FileProvider;
import io.appmetrica.analytics.impl.crash.ndk.crashpad.CrashpadConstants;
import io.appmetrica.analytics.impl.crash.ndk.crashpad.CrashpadCrashWatcher;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class CrashpadListenerImpl implements CrashpadListener {

    @NonNull
    private final List<Consumer<String>> listeners = new ArrayList<Consumer<String>>();
    @NonNull
    private final CrashpadCrashWatcher crashpadCrashWatcher;
    @NonNull
    private final Consumer<String> crashpadConsumer = new Consumer<String>() {
        @Override
        public void consume(@NonNull String input) {
            notifyListeners(input);
        }
    };

    public CrashpadListenerImpl(@NonNull Context context) {
        this(new CrashpadCrashWatcher(
                CrashpadConstants.getCrashpadNewCrashSocketName(context),
                new FileProvider().getStorageSubDirectory(
                        context, CrashpadConstants.APPMETRICA_NATIVE_CRASHES_FOLDER
                )
        ));
    }

    @VisibleForTesting
    CrashpadListenerImpl(@NonNull CrashpadCrashWatcher crashpadCrashWatcher) {
        this.crashpadCrashWatcher = crashpadCrashWatcher;
    }

    @Override
    public synchronized void addListener(@NonNull Consumer<String> listener) {
        listeners.add(listener);
    }

    @Override
    public synchronized void removeListener(@NonNull Consumer<String> listener) {
        listeners.remove(listener);
    }

    @Override
    public void onCreate() {
        crashpadCrashWatcher.subscribe(crashpadConsumer);
    }

    @Override
    public void onDestroy() {
        crashpadCrashWatcher.unsubscribe(crashpadConsumer);
    }

    private void notifyListeners(@NonNull String input) {
        final List<Consumer<String>> localListeners;
        synchronized (this) {
            localListeners = new ArrayList<Consumer<String>>(listeners);
        }
        for (Consumer<String> listener : localListeners) {
            listener.consume(input);
        }
    }
}
