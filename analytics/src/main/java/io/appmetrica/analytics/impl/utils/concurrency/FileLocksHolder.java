package io.appmetrica.analytics.impl.utils.concurrency;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;

public class FileLocksHolder {

    @SuppressLint("StaticFieldLeak")
    @Nullable
    private static volatile FileLocksHolder INSTANCE;

    @NonNull
    private final Context context;
    private final Map<String, ExclusiveMultiProcessFileLock> locks =
            new HashMap<String, ExclusiveMultiProcessFileLock>();

    @NonNull
    public static FileLocksHolder getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (FileLocksHolder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FileLocksHolder(context);
                }
            }
        }
        return INSTANCE;
    }

    @VisibleForTesting
    FileLocksHolder(@NonNull Context context) {
        this.context = context;
    }

    @NonNull
    public ExclusiveMultiProcessFileLock getOrCreate(@NonNull String fileName) {
        if (!locks.containsKey(fileName)) {
            synchronized (this) {
                if (!locks.containsKey(fileName)) {
                    locks.put(fileName, new ExclusiveMultiProcessFileLock(context, fileName));
                }
            }
        }
        return locks.get(fileName);
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    static void clearInstance() {
        INSTANCE = null;
    }
}
