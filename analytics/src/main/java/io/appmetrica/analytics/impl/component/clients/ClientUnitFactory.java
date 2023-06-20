package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.CommonArguments;

public interface ClientUnitFactory<C extends ClientUnit> {

    @NonNull
    C createClientUnit(@NonNull Context context,
                       @NonNull ComponentsRepository repository,
                       @NonNull ClientDescription clientDescription,
                       @NonNull CommonArguments sdkConfig);
}
