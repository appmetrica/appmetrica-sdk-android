package io.appmetrica.analytics.impl.preparer;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;

public class FullNetworkInfoComposer implements NetworkInfoComposer {

    @Nullable
    @Override
    public EventProto.ReportMessage.Session.Event.NetworkInfo getNetworkInfo(@Nullable Integer connectionType,
                                                                             @Nullable String cellularConnectionType) {

        final EventProto.ReportMessage.Session.Event.NetworkInfo networkInfoBuilder =
                new EventProto.ReportMessage.Session.Event.NetworkInfo();

        if (connectionType != null) {
            networkInfoBuilder.connectionType = connectionType;
        }
        if (null != cellularConnectionType) {
            networkInfoBuilder.cellularNetworkType = cellularConnectionType;
        }

        return networkInfoBuilder;
    }
}
