package io.appmetrica.analytics.impl.db.state.converter;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.PermissionsCollectingConfig;

public class PermissionsCollectingConfigConverter implements
        ProtobufConverter<PermissionsCollectingConfig, StartupStateProtobuf.StartupState.PermissionsCollectingConfig> {

    @NonNull
    @Override
    public StartupStateProtobuf.StartupState.PermissionsCollectingConfig fromModel(
            @NonNull PermissionsCollectingConfig value) {
        StartupStateProtobuf.StartupState.PermissionsCollectingConfig protoConfig =
                new StartupStateProtobuf.StartupState.PermissionsCollectingConfig();
        protoConfig.checkIntervalSeconds = value.mCheckIntervalSeconds;
        protoConfig.forceSendIntervalSeconds = value.mForceSendIntervalSeconds;
        return protoConfig;
    }

    @NonNull
    @Override
    public PermissionsCollectingConfig toModel(
            @NonNull StartupStateProtobuf.StartupState.PermissionsCollectingConfig nano) {
        return new PermissionsCollectingConfig(nano.checkIntervalSeconds, nano.forceSendIntervalSeconds);
    }
}
