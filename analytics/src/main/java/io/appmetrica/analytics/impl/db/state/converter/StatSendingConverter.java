package io.appmetrica.analytics.impl.db.state.converter;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.StatSending;

public class StatSendingConverter
        implements ProtobufConverter<StatSending, StartupStateProtobuf.StartupState.StatSending> {
    @NonNull
    @Override
    public StartupStateProtobuf.StartupState.StatSending fromModel(@NonNull StatSending value) {
        StartupStateProtobuf.StartupState.StatSending statSending =
                new StartupStateProtobuf.StartupState.StatSending();
        statSending.disabledReportingInterval = value.disabledReportingInterval;

        return statSending;
    }

    @NonNull
    @Override
    public StatSending toModel(@NonNull StartupStateProtobuf.StartupState.StatSending nano) {
        return new StatSending(nano.disabledReportingInterval);
    }
}
