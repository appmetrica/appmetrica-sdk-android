package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.utils.limitation.SimpleMapLimitation;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.internal.CounterConfigurationReporterType;

class CommutationReporterEnvironment extends ReporterEnvironment {

    public CommutationReporterEnvironment(@NonNull ProcessConfiguration processConfiguration) {
        super(
            processConfiguration,
            new CounterConfiguration(),
            new ErrorEnvironment(
                new SimpleMapLimitation(LoggerStorage.getMainPublicOrAnonymousLogger(), ErrorEnvironment.TAG)
            )
        );
        //todo (avitenko) workaround. CommutationReporterEnvironment does not need CounterConfiguration

        getReporterConfiguration().setReporterType(CounterConfigurationReporterType.COMMUTATION);
    }

    @Override
    boolean isForegroundSessionPaused() {
        return true;
    }

}

