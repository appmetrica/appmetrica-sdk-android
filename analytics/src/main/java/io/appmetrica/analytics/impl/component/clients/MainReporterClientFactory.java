package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.MainReporterComponentId;
import io.appmetrica.analytics.impl.component.MainReporterComponentUnit;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponentFactory;
import io.appmetrica.analytics.impl.startup.StartupUnit;
import io.appmetrica.analytics.impl.startup.executor.RegularExecutorFactory;

class MainReporterClientFactory implements ClientUnitFactory, ComponentUnitFactory<MainReporterComponentUnit> {

    @NonNull
    public ClientUnit createClientUnit(@NonNull Context context,
                                       @NonNull ComponentsRepository repository,
                                       @NonNull ClientDescription clientDescription,
                                       @NonNull CommonArguments sdkConfig) {


        RegularDispatcherComponent componentUnit =
                repository.getOrCreateRegularComponent(
                        new MainReporterComponentId(clientDescription.getPackageName(), clientDescription.getApiKey()),
                        sdkConfig,
                        new RegularDispatcherComponentFactory<MainReporterComponentUnit>(this)
                );

        return new MainReporterClientUnit(
                context,
                componentUnit
        );
    }

    @NonNull
    public MainReporterComponentUnit createComponentUnit(@NonNull Context context,
                                                         @NonNull ComponentId componentId,
                                                         @NonNull CommonArguments.ReporterArguments sdkConfig,
                                                         @NonNull StartupUnit startupUnit) {
        return new MainReporterComponentUnit(
                context,
                startupUnit.getStartupState(),
                componentId,
                sdkConfig,
                GlobalServiceLocator.getInstance().getReferrerHolder(),
                GlobalServiceLocator.getInstance().getDataSendingRestrictionController(),
                new RegularExecutorFactory(startupUnit)
        );
    }
}
