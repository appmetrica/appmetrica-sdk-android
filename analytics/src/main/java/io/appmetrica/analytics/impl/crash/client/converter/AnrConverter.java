package io.appmetrica.analytics.impl.crash.client.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.impl.crash.client.Anr;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;

public class AnrConverter implements ProtobufConverter<Anr, CrashAndroid.Anr> {

    @NonNull
    private final AllThreadsConverter allThreadsConverter;
    @NonNull
    private final CrashOptionalBoolConverter optionalBoolConverter;

    public AnrConverter() {
        this(new AllThreadsConverter(new ThreadStateConverter()), new CrashOptionalBoolConverter());
    }

    @VisibleForTesting
    AnrConverter(@NonNull AllThreadsConverter allThreadsConverter,
                 @NonNull CrashOptionalBoolConverter optionalBoolConverter) {
        this.allThreadsConverter = allThreadsConverter;
        this.optionalBoolConverter = optionalBoolConverter;
    }

    @NonNull
    @Override
    public CrashAndroid.Anr fromModel(@NonNull Anr value) {
        CrashAndroid.Anr outAnr = new CrashAndroid.Anr();
        outAnr.threads = allThreadsConverter.fromModel(value.mAllThreads);
        if (value.mBuildId != null) {
            outAnr.buildId = value.mBuildId;
        }
        outAnr.isOffline = optionalBoolConverter.toProto(value.mIsOffline);
        return outAnr;
    }

    @NonNull
    @Override
    public Anr toModel(@NonNull CrashAndroid.Anr nano) {
        throw new UnsupportedOperationException();
    }
}
