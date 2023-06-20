package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommutationComponentId;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponentFactory;

//For [Idle/Anonymous/Commutation]ReporterEnvironment
public class MainCommutationClientUnitFactory implements ClientUnitFactory {

    @NonNull
    public CommutationClientUnit createClientUnit(@NonNull Context context,
                                                  @NonNull ComponentsRepository repository,
                                                  @NonNull ClientDescription clientDescription,
                                                  @NonNull CommonArguments sdkConfig) {

        CommutationDispatcherComponent componentUnit = repository.getOrCreateCommutationComponent(
                new CommutationComponentId(clientDescription.getPackageName()),
                sdkConfig,
                new CommutationDispatcherComponentFactory()
        );

        return new CommutationClientUnit(
                context,
                componentUnit,
                sdkConfig
        );
    }
}
