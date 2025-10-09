package io.appmetrica.analytics.impl.preloadinfo;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.data.BaseProtobufStateSerializer;
import io.appmetrica.analytics.impl.protobuf.client.PreloadInfoProto;
import java.io.IOException;

public class PreloadInfoDataSerializer extends BaseProtobufStateSerializer<PreloadInfoProto.PreloadInfoData> {
    @NonNull
    @Override
    public PreloadInfoProto.PreloadInfoData toState(@NonNull byte[] data) throws IOException {
        return PreloadInfoProto.PreloadInfoData.parseFrom(data);
    }

    @NonNull
    @Override
    public PreloadInfoProto.PreloadInfoData defaultValue() {
        return new PreloadInfoProto.PreloadInfoData();
    }
}
