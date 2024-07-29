package io.appmetrica.analytics.impl.crash.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.backport.BiFunction;
import io.appmetrica.analytics.impl.crash.jvm.client.ThreadState;
import java.util.Arrays;

public class FullStateConverter implements BiFunction<Thread, StackTraceElement[], ThreadState> {

    @NonNull
    @Override
    public ThreadState apply(@NonNull Thread first,
                            @Nullable StackTraceElement[] stackTrace) {
        return new ThreadState(
                first.getName(),
                first.getPriority(),
                first.getId(),
                getThreadGroupName(first),
                first.getState().ordinal(),
                stackTrace == null ? null : Arrays.asList(stackTrace)
        );
    }

    @NonNull
    static String getThreadGroupName(@NonNull Thread thread) {
        final ThreadGroup threadGroup = thread.getThreadGroup();
        return threadGroup != null ? threadGroup.getName(): "";
    }

}
