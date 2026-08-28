package io.appmetrica.analytics.impl.preparer;

import io.appmetrica.analytics.impl.protobuf.backend.EventProto;

public class NoneEncodingTypeProvider implements EncodingTypeProvider {

    @Override
    public int getEncodingType() {
        return EventProto.ReportMessage.Session.Event.NONE;
    }
}
