package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.SelfReportingArgumentsFactory;
import io.appmetrica.analytics.impl.component.SelfSdkReportingComponentUnit;
import io.appmetrica.analytics.impl.startup.StartupUnit;
import io.appmetrica.analytics.impl.startup.executor.StubbedExecutorFactory;

class SelfSdkReportingFactory extends ReporterClientUnitFactory {

    @NonNull
    @Override
    public ComponentUnit createComponentUnit(@NonNull Context context,
                                             @NonNull ComponentId componentId,
                                             @NonNull CommonArguments.ReporterArguments sdkConfig,
                                             @NonNull StartupUnit startupUnit) {
        return new SelfSdkReportingComponentUnit(
                context,
                startupUnit.getStartupState(),
                componentId,
                sdkConfig,
                new SelfReportingArgumentsFactory(
                        GlobalServiceLocator.getInstance().getStatisticsRestrictionController()
                ),
                new StubbedExecutorFactory()
        );
    }
}
