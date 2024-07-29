package io.appmetrica.analytics.impl.crash.jvm.converter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.impl.crash.jvm.client.ThrowableModel;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import java.util.List;

public class ThrowableConverter implements ProtobufConverter<ThrowableModel, CrashAndroid.Throwable> {

    @NonNull
    private final StackTraceConverter backtraceConverter;

    public ThrowableConverter() {
        this(new StackTraceConverter());
    }

    @VisibleForTesting
    ThrowableConverter(@NonNull StackTraceConverter backtraceConverter) {
        this.backtraceConverter = backtraceConverter;
    }

    @NonNull
    @Override
    public CrashAndroid.Throwable fromModel(@NonNull ThrowableModel value) {
        CrashAndroid.Throwable outState = new CrashAndroid.Throwable();
        outState.exceptionClass = WrapUtils.getOrDefault(value.getExceptionClass(), "");
        outState.message = StringUtils.correctIllFormedString(WrapUtils.getOrDefault(value.getMessage(), ""));
        if (value.getStacktrace() != null) {
            outState.backtrace = backtraceConverter.fromModel(value.getStacktrace());
        }
        if (value.getCause() != null) {
            outState.cause = fromModel(value.getCause());
        }
        fillSuppressedExceptions(outState, value.getSuppressed());
        return outState;
    }

    @NonNull
    @Override
    public ThrowableModel toModel(@NonNull CrashAndroid.Throwable nano) {
        throw new UnsupportedOperationException();
    }

    private void fillSuppressedExceptions(@NonNull CrashAndroid.Throwable proto,
                                          @Nullable List<ThrowableModel> suppressedArray) {
        if (suppressedArray == null) {
            proto.suppressed = new CrashAndroid.Throwable[0];
        } else {
            proto.suppressed = new CrashAndroid.Throwable[suppressedArray.size()];
            int index = 0;
            for (ThrowableModel suppressed : suppressedArray) {
                proto.suppressed[index++] = fromModel(suppressed);
            }
        }
    }
}
