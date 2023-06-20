package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.ac.CrashpadServiceHelper;
import java.util.LinkedList;
import java.util.List;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class CrashpadCrashWatcher {

    private static final String TAG = "[CrashpadCrashWatcher]";

    @NonNull
    private final String socketName;

    private LocalServerSocket socket;

    private volatile boolean isRunning = false;

    @NonNull
    private final CrashpadLoader libraryLoader;
    @NonNull
    private final Consumer<String> nativeCrashSetuper;
    @Nullable
    private final String crashpadDbDirectory;

    private List<Consumer<String>> consumers = new LinkedList<Consumer<String>>();

    private final Thread watcherThread = new Thread() {
        @Override
        public void run() {
            while (isRunning) {
                try {
                    LocalSocket client = socket.accept();
                    byte[] buff = new byte[256];
                    int bytes = client.getInputStream().read(buff);
                    client.close();
                    byte[] stringData = new byte[bytes];
                    System.arraycopy(buff, 0, stringData, 0, bytes);
                    onNewCrash(new String(stringData));
                } catch (Throwable e) {
                    YLogger.e(e,"%s error reading data", TAG);
                }
            }
        }
    };

    public CrashpadCrashWatcher(@NonNull String socketName, @Nullable String crashpadDbDirectory) {
        this(socketName, crashpadDbDirectory, CrashpadLoader.getInstance(), new Consumer<String>() {
            @Override
            public void consume(@NonNull String folder) {
                CrashpadServiceHelper.setUpServiceHelper(folder);
            }
        });
    }

    public void subscribe(@NonNull Consumer<String> crashConsumer) {
        YLogger.d("%s start watcher. crashpadDbDirectory: %s", TAG, crashpadDbDirectory);
        synchronized (this) {
            consumers.add(crashConsumer);
        }
        if (!isRunning && crashpadDbDirectory != null) {
            synchronized (this) {
                // close() does not interrupt accept() and this name become "forever in use"
                if (!isRunning) {
                    try {
                        if (libraryLoader.loadIfNeeded()) {
                            YLogger.debug(TAG, "open socket");
                            socket = new LocalServerSocket(socketName);
                            isRunning = true;
                            nativeCrashSetuper.consume(crashpadDbDirectory);
                            watcherThread.start();
                        }
                    } catch (Throwable exception) {
                        YLogger.error(TAG, "can't start crashpad socket %s", exception);
                    }
                }
            }
        }
    }

    public synchronized void unsubscribe(@NonNull Consumer<String> crashConsumer) {
        consumers.remove(crashConsumer);
    }

    // Synchronize the whole onNewCrash() method and unsubscribe() to leave only two scenarios:
    // pause ComonentUnits destroying and send crash to the current session or just skip it.
    private synchronized void onNewCrash(@NonNull String uuid) {
        YLogger.d("%s deliver new native crash from crashpad %s to %d subscribers", TAG, uuid, consumers.size());
        for (Consumer<String> consumer: consumers) {
            consumer.consume(uuid);
        }
    }

    @VisibleForTesting
    CrashpadCrashWatcher(@NonNull String socketName,
                         @Nullable String crashpadDbDirectory,
                         @NonNull CrashpadLoader libraryLoader,
                         @NonNull Consumer<String> nativeCrashSetuper
    ) {
        this.socketName = socketName;
        this.crashpadDbDirectory = crashpadDbDirectory;
        this.libraryLoader = libraryLoader;
        this.nativeCrashSetuper = nativeCrashSetuper;
    }

}
