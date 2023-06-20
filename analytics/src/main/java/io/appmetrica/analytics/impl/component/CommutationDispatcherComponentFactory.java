package io.appmetrica.analytics.impl.component;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.startup.StartupCenter;

public class CommutationDispatcherComponentFactory
        implements DispatcherComponentFactory<CommutationDispatcherComponent> {

    @Override
    public CommutationDispatcherComponent createDispatcherComponent(@NonNull Context context,
                                                                    @NonNull ComponentId componentId,
                                                                    @NonNull CommonArguments clientConfiguration) {
        return new CommutationDispatcherComponent(
                context,
                StartupCenter.getInstance(),
                componentId,
                clientConfiguration,
                GlobalServiceLocator.getInstance().getReferrerHolder()
        );
    }
}
