package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;

public class EventStartConverter
        implements ProtobufConverter<EventStart, io.appmetrica.analytics.impl.protobuf.backend.EventStart.Value> {

    @NonNull
    @Override
    public io.appmetrica.analytics.impl.protobuf.backend.EventStart.Value fromModel(@NonNull EventStart value) {
        io.appmetrica.analytics.impl.protobuf.backend.EventStart.Value proto =
                new io.appmetrica.analytics.impl.protobuf.backend.EventStart.Value();
        if (value.buildId != null) {
            proto.buildId = value.buildId.getBytes();
        }
        return proto;
    }

    @NonNull
    @Override
    public EventStart toModel(@NonNull io.appmetrica.analytics.impl.protobuf.backend.EventStart.Value nano) {
        return new EventStart(new String(nano.buildId));
    }
}
