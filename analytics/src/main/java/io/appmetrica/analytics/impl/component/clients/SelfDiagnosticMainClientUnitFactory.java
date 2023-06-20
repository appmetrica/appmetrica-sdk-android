package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.MainReporterComponentId;

public class SelfDiagnosticMainClientUnitFactory extends SelfDiagnosticClientUnitFactory {

    @NonNull
    @Override
    public ClientUnit createClientUnit(@NonNull Context context,
                                       @NonNull ComponentsRepository repository,
                                       @NonNull ClientDescription clientDescription,
                                       @NonNull CommonArguments sdkConfig) {
        return super.createClientUnit(
                new MainReporterComponentId(clientDescription.getPackageName(), clientDescription.getApiKey()),
                repository
        );
    }
}
