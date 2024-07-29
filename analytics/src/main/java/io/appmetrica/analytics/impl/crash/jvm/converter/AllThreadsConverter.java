package io.appmetrica.analytics.impl.crash.jvm.converter;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.impl.crash.jvm.client.AllThreads;
import io.appmetrica.analytics.impl.crash.jvm.client.ThreadState;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;

public class AllThreadsConverter implements ProtobufConverter<AllThreads, CrashAndroid.AllThreads> {

    @NonNull
    private ThreadStateConverter threadStateConverter;

    public AllThreadsConverter(@NonNull ThreadStateConverter threadStateConverter) {
        this.threadStateConverter = threadStateConverter;
    }

    @NonNull
    @Override
    public CrashAndroid.AllThreads fromModel(@NonNull AllThreads inState) {
        CrashAndroid.AllThreads outState = new CrashAndroid.AllThreads();
        if (inState.affectedThread != null) {
            outState.affectedThread = threadStateConverter.fromModel(inState.affectedThread);
        }
        outState.threads = new CrashAndroid.Thread[inState.threads.size()];
        int i = 0;
        for (ThreadState state : inState.threads) {
            outState.threads[i] = threadStateConverter.fromModel(state);
            i++;
        }
        if (inState.processName != null) {
            outState.processName = inState.processName;
        }
        return outState;
    }

    @NonNull
    @Override
    public AllThreads toModel(@NonNull CrashAndroid.AllThreads nano) {
        throw new UnsupportedOperationException();
    }
}
