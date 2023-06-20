package io.appmetrica.analytics.impl.db.state.converter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.impl.OptionalBoolConverter;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.StartupOptionalBoolConverter;

public class FlagsConverter implements
        ProtobufConverter<CollectingFlags, StartupStateProtobuf.StartupState.Flags> {

    @NonNull
    private final OptionalBoolConverter optionalBoolConverter;

    public FlagsConverter() {
        this(new StartupOptionalBoolConverter());
    }

    @VisibleForTesting
    FlagsConverter(@NonNull OptionalBoolConverter optionalBoolConverter) {
        this.optionalBoolConverter= optionalBoolConverter;
    }

    @NonNull
    @Override
    public StartupStateProtobuf.StartupState.Flags fromModel(@NonNull CollectingFlags value) {
        StartupStateProtobuf.StartupState.Flags flags = new StartupStateProtobuf.StartupState.Flags();
        flags.featuresCollectingEnabled = value.featuresCollectingEnabled;
        flags.permissionsCollectingEnabled = value.permissionsCollectingEnabled;
        flags.googleAid = value.googleAid;
        flags.simInfo = value.simInfo;
        flags.huaweiOaid = value.huaweiOaid;
        flags.sslPinning = optionalBoolConverter.toProto(value.sslPinning);
        return flags;
    }

    @NonNull
    @Override
    public CollectingFlags toModel(@NonNull StartupStateProtobuf.StartupState.Flags nano) {
        return new CollectingFlags.CollectingFlagsBuilder()
                .withSimInfo(nano.simInfo)
                .withGoogleAid(nano.googleAid)
                .withFeaturesCollectingEnabled(nano.featuresCollectingEnabled)
                .withPermissionsCollectingEnabled(nano.permissionsCollectingEnabled)
                .withHuaweiOaid(nano.huaweiOaid)
                .withSslPinning(optionalBoolConverter.toModel(nano.sslPinning))
                .build();
    }
}
