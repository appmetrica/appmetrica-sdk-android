package io.appmetrica.analytics.impl.db.state.converter;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.CacheControl;

public class CacheControlConverter
        implements ProtobufConverter<CacheControl, StartupStateProtobuf.StartupState.CacheControl> {

    @NonNull
    @Override
    public StartupStateProtobuf.StartupState.CacheControl fromModel(@NonNull CacheControl value) {
        StartupStateProtobuf.StartupState.CacheControl nano = new StartupStateProtobuf.StartupState.CacheControl();
        nano.lastKnownLocationTtl = value.lastKnownLocationTtl;
        return nano;
    }

    @NonNull
    @Override
    public CacheControl toModel(@NonNull StartupStateProtobuf.StartupState.CacheControl nano) {
        return new CacheControl(
                nano.lastKnownLocationTtl
        );
    }
}
