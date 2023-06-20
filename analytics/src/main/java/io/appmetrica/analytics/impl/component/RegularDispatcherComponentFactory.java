package io.appmetrica.analytics.impl.component;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.component.clients.ComponentUnitFactory;

public class RegularDispatcherComponentFactory<COMPONENT extends IReportableComponent &
        IComponent>
        implements DispatcherComponentFactory<RegularDispatcherComponent>{

    @NonNull
    private final ComponentUnitFactory<COMPONENT> mComponentUnitFactory;

    public RegularDispatcherComponentFactory(@NonNull ComponentUnitFactory<COMPONENT> componentUnitFactory) {
        mComponentUnitFactory = componentUnitFactory;
    }

    @Override
    public RegularDispatcherComponent createDispatcherComponent(@NonNull Context context,
                                                                @NonNull ComponentId componentId,
                                                                @NonNull CommonArguments arguments) {

        return new RegularDispatcherComponent<COMPONENT>(
                context,
                componentId,
                arguments,
                mComponentUnitFactory
        );
    }

    @VisibleForTesting
    @NonNull
    public ComponentUnitFactory<COMPONENT> getComponentUnitFactory() {
        return mComponentUnitFactory;
    }
}
