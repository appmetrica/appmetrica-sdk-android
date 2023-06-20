package io.appmetrica.analytics.impl.billing;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.db.protobuf.BaseProtobufStateSerializer;
import io.appmetrica.analytics.impl.protobuf.client.AutoInappCollectingInfoProto;
import java.io.IOException;

public class AutoInappCollectingInfoSerializer extends
        BaseProtobufStateSerializer<AutoInappCollectingInfoProto.AutoInappCollectingInfo> {

    @NonNull
    @Override
    public AutoInappCollectingInfoProto.AutoInappCollectingInfo toState(@NonNull byte[] data) throws IOException {
        return AutoInappCollectingInfoProto.AutoInappCollectingInfo.parseFrom(data);
    }

    @NonNull
    @Override
    public AutoInappCollectingInfoProto.AutoInappCollectingInfo defaultValue() {
        return new AutoInappCollectingInfoProto.AutoInappCollectingInfo();
    }
}
