package io.appmetrica.analytics.impl.startup.executor;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.IComponent;
import io.appmetrica.analytics.impl.startup.StartupUnit;

public class RegularExecutorFactory extends ComponentStartupExecutorFactory {

    public RegularExecutorFactory(@NonNull StartupUnit startupUnit) {
        super(startupUnit);
    }

    @Override
    public <C extends IComponent> StartupExecutor create() {
        return new RegularStartupExecutor(startupUnit);
    }
}
