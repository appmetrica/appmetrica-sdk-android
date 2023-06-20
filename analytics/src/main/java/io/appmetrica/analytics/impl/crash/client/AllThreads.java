package io.appmetrica.analytics.impl.crash.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import java.util.Collections;
import java.util.List;

public class AllThreads {

    @Nullable
    public final ThreadState affectedThread;
    @NonNull
    public final List<ThreadState> threads;
    @Nullable
    public final String processName;

    public AllThreads(@Nullable ThreadState affectedThread,
                      @Nullable List<ThreadState> threads,
                      @Nullable String processName) {
        this.affectedThread = affectedThread;
        this.threads = threads == null ?
            Collections.<ThreadState>emptyList() : CollectionUtils.unmodifiableListCopy(threads);
        this.processName = processName;
    }

    public AllThreads(@Nullable String processName) {
        this(null, null, processName);
    }

}
