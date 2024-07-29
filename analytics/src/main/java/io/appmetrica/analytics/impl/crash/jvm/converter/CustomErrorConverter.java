package io.appmetrica.analytics.impl.crash.jvm.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.crash.jvm.client.CustomError;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;

public class CustomErrorConverter implements ProtobufConverter<CustomError, CrashAndroid.Error> {

    @NonNull
    private final RegularErrorConverter regularErrorConverter;

    public CustomErrorConverter() {
        this(new RegularErrorConverter());
    }

    @VisibleForTesting
    CustomErrorConverter(@NonNull RegularErrorConverter regularErrorConverter) {
        this.regularErrorConverter = regularErrorConverter;
    }

    @NonNull
    @Override
    public CrashAndroid.Error fromModel(@NonNull CustomError value) {
        CrashAndroid.Error error = regularErrorConverter.fromModel(value.regularError);
        error.type = CrashAndroid.Error.CUSTOM;
        error.custom = new CrashAndroid.Error.Custom();
        error.custom.identifier = StringUtils.correctIllFormedString(value.identifier);
        return error;
    }

    @NonNull
    @Override
    public CustomError toModel(@NonNull CrashAndroid.Error nano) {
        throw new UnsupportedOperationException();
    }
}
