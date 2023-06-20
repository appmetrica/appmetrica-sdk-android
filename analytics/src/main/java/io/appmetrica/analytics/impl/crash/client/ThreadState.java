package io.appmetrica.analytics.impl.crash.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import java.util.Collections;
import java.util.List;

public class ThreadState {

    @NonNull
    public final String name;
    public final int priority;
    public final long tid;
    @NonNull
    public final String group;
    @Nullable
    public final Integer state;
    @NonNull
    public final List<StackTraceElement> stacktrace;

    public ThreadState(@NonNull String name,
                       int priority, long tid,
                       @NonNull String group,
                       @Nullable Integer state,
                       @Nullable List<StackTraceElement> stacktrace) {
        this.name = name;
        this.priority = priority;
        this.tid = tid;
        this.group = group;
        this.state = state;
        this.stacktrace = stacktrace == null ?
                Collections.<StackTraceElement>emptyList() : CollectionUtils.unmodifiableListCopy(stacktrace);
    }

}
