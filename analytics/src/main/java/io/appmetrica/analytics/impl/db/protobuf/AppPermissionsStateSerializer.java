package io.appmetrica.analytics.impl.db.protobuf;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.protobuf.client.AppPermissionsStateProtobuf;
import java.io.IOException;

public class AppPermissionsStateSerializer extends
        BaseProtobufStateSerializer<AppPermissionsStateProtobuf.AppPermissionsState> {

    @NonNull
    @Override
    public AppPermissionsStateProtobuf.AppPermissionsState toState(@NonNull byte[] data) throws IOException {
        return AppPermissionsStateProtobuf.AppPermissionsState.parseFrom(data);
    }

    @NonNull
    @Override
    public AppPermissionsStateProtobuf.AppPermissionsState defaultValue() {
        return new AppPermissionsStateProtobuf.AppPermissionsState();
    }
}
