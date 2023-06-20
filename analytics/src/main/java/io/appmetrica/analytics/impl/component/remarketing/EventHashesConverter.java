package io.appmetrica.analytics.impl.component.remarketing;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.impl.protobuf.client.Eventhashes;

class EventHashesConverter implements ProtobufConverter<EventHashes, Eventhashes.EventHashes> {

    @NonNull
    @Override
    public Eventhashes.EventHashes fromModel(@NonNull EventHashes value) {
        Eventhashes.EventHashes eventHashes = new Eventhashes.EventHashes();
        eventHashes.eventNameHashes = new int[value.getEventNameHashes().size()];
        int i = 0;
        for (Integer eventNameHash : value.getEventNameHashes()) {
            eventHashes.eventNameHashes[i] = eventNameHash;
            i++;
        }
        eventHashes.hashesCountFromPreviousVersion = value.getHashesCountFromLastVersion();
        eventHashes.lastVersionCode = value.getLastVersionCode();
        eventHashes.treatUnknownEventAsNew = value.treatUnknownEventAsNew();
        return eventHashes;
    }

    @NonNull
    @Override
    public EventHashes toModel(@NonNull Eventhashes.EventHashes nano) {
        EventHashes eventHashes = new EventHashes(
                nano.treatUnknownEventAsNew,
                nano.lastVersionCode,
                nano.hashesCountFromPreviousVersion,
                nano.eventNameHashes
        );
        return eventHashes;
    }

}
