package io.appmetrica.analytics.impl.component.clients;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.ComponentId;

public abstract class SelfDiagnosticClientUnitFactory implements ClientUnitFactory {

    @NonNull
    protected ClientUnit createClientUnit(@NonNull ComponentId componentId,
                                          @NonNull ComponentsRepository repository) {
        return new SelfDiagnosticClientUnit(repository.getRegularComponentIfExists(componentId));
    }

}
