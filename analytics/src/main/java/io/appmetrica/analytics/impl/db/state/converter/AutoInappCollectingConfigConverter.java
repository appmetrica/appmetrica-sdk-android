package io.appmetrica.analytics.impl.db.state.converter;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;

public class AutoInappCollectingConfigConverter implements
        ProtobufConverter<BillingConfig, StartupStateProtobuf.StartupState.AutoInappCollectingConfig> {
    @NonNull
    @Override
    public StartupStateProtobuf.StartupState.AutoInappCollectingConfig fromModel(@NonNull BillingConfig value) {

        StartupStateProtobuf.StartupState.AutoInappCollectingConfig nano =
                new StartupStateProtobuf.StartupState.AutoInappCollectingConfig();

        nano.sendFrequencySeconds = value.sendFrequencySeconds;
        nano.firstCollectingInappMaxAgeSeconds = value.firstCollectingInappMaxAgeSeconds;

        return nano;
    }

    @NonNull
    @Override
    public BillingConfig toModel(@NonNull StartupStateProtobuf.StartupState.AutoInappCollectingConfig nano) {
        return new BillingConfig(
                nano.sendFrequencySeconds,
                nano.firstCollectingInappMaxAgeSeconds
        );
    }
}
