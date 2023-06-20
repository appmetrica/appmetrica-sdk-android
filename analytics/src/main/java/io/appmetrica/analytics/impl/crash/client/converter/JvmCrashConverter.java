package io.appmetrica.analytics.impl.crash.client.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.Converter;
import io.appmetrica.analytics.impl.crash.client.UnhandledException;

public class JvmCrashConverter implements Converter<UnhandledException, byte[]> {

    @NonNull
    private final ModelToByteArraySerializer<UnhandledException> baseExceptionConverter;

    public JvmCrashConverter() {
        this(new ModelToByteArraySerializer<>(new UnhandledExceptionConverter()));
    }

    @VisibleForTesting
    JvmCrashConverter(@NonNull ModelToByteArraySerializer<UnhandledException> baseExceptionConverter) {
        this.baseExceptionConverter = baseExceptionConverter;
    }

    @NonNull
    @Override
    public byte[] fromModel(@NonNull UnhandledException value) {
        return baseExceptionConverter.toProto(value);
    }

    @NonNull
    @Override
    public UnhandledException toModel(@NonNull byte[] nano) {
        throw new UnsupportedOperationException();
    }
}
