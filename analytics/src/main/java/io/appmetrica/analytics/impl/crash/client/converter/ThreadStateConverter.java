package io.appmetrica.analytics.impl.crash.client.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.crash.client.ThreadState;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;

public class ThreadStateConverter implements ProtobufConverter<ThreadState, CrashAndroid.Thread> {

    @NonNull
    private final StackTraceConverter stackTraceConverter;

    public ThreadStateConverter() {
        this(new StackTraceConverter());
    }

    @VisibleForTesting
    ThreadStateConverter(@NonNull StackTraceConverter stackTraceConverter) {
        this.stackTraceConverter = stackTraceConverter;
    }

    @NonNull
    @Override
    public CrashAndroid.Thread fromModel(@NonNull ThreadState value) {
        CrashAndroid.Thread thread = new CrashAndroid.Thread();
        thread.state = value.state == null ? -1 : value.state;
        thread.group = value.group;
        thread.priority = value.priority;
        thread.name = value.name;
        thread.tid = value.tid;
        thread.stacktrace = stackTraceConverter.fromModel(Utils.convertStackTraceToInternal(value.stacktrace));
        return thread;
    }

    @NonNull
    @Override
    public ThreadState toModel(@NonNull CrashAndroid.Thread nano) {
        throw new UnsupportedOperationException();
    }

}
