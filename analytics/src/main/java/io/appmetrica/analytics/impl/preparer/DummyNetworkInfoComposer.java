package io.appmetrica.analytics.impl.preparer;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;

public class DummyNetworkInfoComposer implements NetworkInfoComposer {

    @Nullable
    @Override
    public EventProto.ReportMessage.Session.Event.NetworkInfo getNetworkInfo(@Nullable Integer connectionType,
                                                                             @Nullable String cellularConnectionType) {
        return null;
    }
}
