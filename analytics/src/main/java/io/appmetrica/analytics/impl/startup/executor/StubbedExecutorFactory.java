package io.appmetrica.analytics.impl.startup.executor;

import io.appmetrica.analytics.impl.component.IComponent;

public class StubbedExecutorFactory extends ComponentStartupExecutorFactory {

    public StubbedExecutorFactory() {
        super(null);
    }

    @Override
    public <C extends IComponent> StartupExecutor create() {
        return new StubbedStartupExecutor();
    }

}
