package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.internal.CounterConfiguration;

class CommutationReporterEnvironment extends ReporterEnvironment {

    public CommutationReporterEnvironment(@NonNull ProcessConfiguration processConfiguration) {
        super(processConfiguration, new CounterConfiguration());
        //todo (avitenko) workaround. CommutationReporterEnvironment does not need CounterConfiguration

        getReporterConfiguration().setReporterType(CounterConfigurationReporterType.COMMUTATION);
    }

    @Override
    boolean isForegroundSessionPaused() {
        return true;
    }

}

