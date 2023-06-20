package io.appmetrica.analytics.impl.component.remarketing;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.db.protobuf.BaseProtobufStateSerializer;
import io.appmetrica.analytics.impl.protobuf.client.Eventhashes;
import java.io.IOException;

public class EventHashesSerializer extends BaseProtobufStateSerializer<Eventhashes.EventHashes> {

    @Override
    @NonNull
    public Eventhashes.EventHashes toState(@NonNull final byte[] data) throws IOException {
        return Eventhashes.EventHashes.parseFrom(data);
    }

    @Override
    @NonNull
    public Eventhashes.EventHashes defaultValue() {
        Eventhashes.EventHashes result = new Eventhashes.EventHashes();
        return result;
    }

}
