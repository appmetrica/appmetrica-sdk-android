package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.DispatcherComponentFactory;
import io.appmetrica.analytics.impl.component.IDispatcherComponent;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.HashMap;
import java.util.Map;

public class ComponentsRepository {
    private static final String TAG = "[ComponentsRepository]";

    private final HashMap<String, RegularDispatcherComponent> mRegularConnectedComponents =
            new HashMap<String, RegularDispatcherComponent>();
    private final HashMap<String, CommutationDispatcherComponent> mCommutationConnectedComponents =
            new HashMap<String, CommutationDispatcherComponent>();

    private final Context mContext;

    public ComponentsRepository(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    @Nullable
    public synchronized RegularDispatcherComponent getRegularComponentIfExists(@NonNull ComponentId componentId) {
        return mRegularConnectedComponents.get(componentId.toString());
    }

    @NonNull
    public synchronized RegularDispatcherComponent getOrCreateRegularComponent(
            @NonNull ComponentId componentId,
            @NonNull CommonArguments clientConfiguration,
            @NonNull DispatcherComponentFactory<RegularDispatcherComponent> factory
    ) {
        return getOrCreateSomeComponent(componentId, clientConfiguration, factory, mRegularConnectedComponents);
    }

    @NonNull
    public synchronized CommutationDispatcherComponent getOrCreateCommutationComponent(
            @NonNull ComponentId componentId,
            @NonNull CommonArguments clientConfiguration,
            @NonNull DispatcherComponentFactory<CommutationDispatcherComponent> factory
    ) {
        return getOrCreateSomeComponent(componentId, clientConfiguration, factory, mCommutationConnectedComponents);
    }

    @NonNull
    private<T extends IDispatcherComponent> T getOrCreateSomeComponent(@NonNull ComponentId componentId,
                                                                       @NonNull CommonArguments clientConfiguration,
                                                                       @NonNull DispatcherComponentFactory<T> factory,
                                                                       @NonNull Map<String, T> components) {
        T component = components.get(componentId.toString());
        if (component == null) {
            component = factory.createDispatcherComponent(
                    mContext,
                    componentId,
                    clientConfiguration
            );
            components.put(componentId.toString(), component);
            YLogger.d(
                    "%sNew %s, component with Id: %s. Size: %d",
                    TAG,
                    component.getClass().getSimpleName(),
                    componentId,
                    components.size()
            );
        } else {
            component.updateConfig(clientConfiguration);
        }
        return component;
    }
}
