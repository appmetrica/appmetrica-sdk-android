package io.appmetrica.analytics.impl.startup.executor;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.IComponent;
import io.appmetrica.analytics.impl.startup.StartupUnit;

public abstract class ComponentStartupExecutorFactory {

    @NonNull
    final StartupUnit startupUnit;

    protected ComponentStartupExecutorFactory(@NonNull StartupUnit startupUnit) {
        this.startupUnit = startupUnit;
    }

    public abstract <C extends IComponent> StartupExecutor create();

}
