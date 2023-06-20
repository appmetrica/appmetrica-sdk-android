package io.appmetrica.analytics.impl.preparer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.ProtobufUtils;

public class SameEventTypeComposer implements EventTypeComposer {

    @Nullable
    @Override
    public Integer getEventType(@NonNull EventFromDbModel event) {
        return ProtobufUtils.internalEventToProto(event.getEventType());
    }
}
