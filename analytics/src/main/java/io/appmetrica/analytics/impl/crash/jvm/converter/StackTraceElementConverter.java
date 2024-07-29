package io.appmetrica.analytics.impl.crash.jvm.converter;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.impl.crash.jvm.client.StackTraceItemInternal;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;

public class StackTraceElementConverter
        implements ProtobufConverter<StackTraceItemInternal, CrashAndroid.StackTraceElement> {

    @NonNull
    @Override
    public CrashAndroid.StackTraceElement fromModel(@NonNull StackTraceItemInternal value) {
        CrashAndroid.StackTraceElement outState = new CrashAndroid.StackTraceElement();
        outState.className = WrapUtils.getOrDefault(value.getClassName(), outState.className);
        outState.fileName = WrapUtils.getOrDefault(value.getFileName(), outState.fileName);
        outState.lineNumber = WrapUtils.getOrDefault(value.getLine(), outState.lineNumber);
        outState.columnNumber = WrapUtils.getOrDefault(value.getColumn(), outState.columnNumber);
        outState.methodName = WrapUtils.getOrDefault(value.getMethodName(), outState.methodName);
        outState.isNative = WrapUtils.getOrDefault(value.isAndroidNative(), outState.isNative);
        return outState;
    }

    @NonNull
    @Override
    public StackTraceItemInternal toModel(@NonNull CrashAndroid.StackTraceElement nano) {
        throw new UnsupportedOperationException();
    }
}
