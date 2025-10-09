package io.appmetrica.analytics.impl.db.protobuf;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.data.BaseProtobufStateSerializer;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import java.io.IOException;

public class StartupStateSerializer extends BaseProtobufStateSerializer<StartupStateProtobuf.StartupState> {

    @NonNull
    @Override
    public StartupStateProtobuf.StartupState toState(@NonNull final byte[] data) throws IOException {
        return StartupStateProtobuf.StartupState.parseFrom(data);
    }

    @NonNull
    @Override
    public StartupStateProtobuf.StartupState defaultValue() {
        StartupStateProtobuf.StartupState state = new StartupStateProtobuf.StartupState();
        state.flags = new StartupStateProtobuf.StartupState.Flags();
        return state;
    }
}
