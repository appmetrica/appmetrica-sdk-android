package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponentFactory;
import io.appmetrica.analytics.impl.component.ReporterComponentUnit;
import io.appmetrica.analytics.impl.startup.StartupUnit;
import io.appmetrica.analytics.impl.startup.executor.RegularExecutorFactory;

public class ReporterClientUnitFactory implements ClientUnitFactory,
        ComponentUnitFactory<ComponentUnit> {

    @NonNull
    public ClientUnit createClientUnit(@NonNull Context context,
                                       @NonNull ComponentsRepository repository,
                                       @NonNull ClientDescription clientDescription,
                                       @NonNull CommonArguments sdkConfig) {


        RegularDispatcherComponent componentUnit =
                repository.getOrCreateRegularComponent(
                        new ComponentId(clientDescription.getPackageName(), clientDescription.getApiKey()),
                        sdkConfig,
                        new RegularDispatcherComponentFactory<ComponentUnit>(this)
                );

        return new RegularClientUnit(
                context,
                componentUnit
        );
    }

    @NonNull
    public ComponentUnit createComponentUnit(@NonNull Context context,
                                             @NonNull ComponentId componentId,
                                             @NonNull CommonArguments.ReporterArguments sdkConfig,
                                             @NonNull StartupUnit startupUnit) {
        return new ReporterComponentUnit(
                context,
                componentId,
                sdkConfig,
                GlobalServiceLocator.getInstance().getDataSendingRestrictionController(),
                startupUnit.getStartupState(),
                new RegularExecutorFactory(startupUnit)
        );
    }
}
