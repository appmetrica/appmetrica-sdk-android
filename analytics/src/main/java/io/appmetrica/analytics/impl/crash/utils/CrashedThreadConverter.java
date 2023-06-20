package io.appmetrica.analytics.impl.crash.utils;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.impl.crash.client.ThreadState;

public class CrashedThreadConverter implements Function<Thread, ThreadState> {

    @NonNull
    @Override
    public ThreadState apply(Thread thread) {
        return new ThreadState(
                thread.getName(),
                thread.getPriority(),
                thread.getId(),
                FullStateConverter.getThreadGroupName(thread),
                null,
                null
        );
    }
}
