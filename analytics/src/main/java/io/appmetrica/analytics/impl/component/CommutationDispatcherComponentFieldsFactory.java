package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.LifecycleDependentComponentManager;
import io.appmetrica.analytics.impl.TaskProcessor;
import io.appmetrica.analytics.impl.component.processor.CommutationProcessingStrategyFactory;
import io.appmetrica.analytics.impl.component.processor.commutation.CommutationHandler;
import io.appmetrica.analytics.impl.component.processor.commutation.CommutationReportProcessor;
import io.appmetrica.analytics.impl.startup.StartupUnit;
import io.appmetrica.analytics.impl.startup.executor.RegularStartupExecutor;

public class CommutationDispatcherComponentFieldsFactory {

    @NonNull
    private final LifecycleDependentComponentManager lifecycleDependentComponentManager;

    CommutationDispatcherComponentFieldsFactory() {
        this(GlobalServiceLocator.getInstance().getLifecycleDependentComponentManager());
    }

    @VisibleForTesting
    CommutationDispatcherComponentFieldsFactory(
            @NonNull LifecycleDependentComponentManager lifecycleDependentComponentManager
    ) {
        this.lifecycleDependentComponentManager = lifecycleDependentComponentManager;
    }

    @NonNull
    CommutationReportProcessor<CommutationHandler, CommutationDispatcherComponent> createCommutationReportProcessor(
            @NonNull CommutationDispatcherComponent component
    ) {
        return new CommutationReportProcessor<CommutationHandler, CommutationDispatcherComponent>(
                new CommutationProcessingStrategyFactory(component), component
        );
    }

    @NonNull
    TaskProcessor<CommutationDispatcherComponent> createTaskProcessor(
            @NonNull CommutationDispatcherComponent component,
            @NonNull StartupUnit startupUnit
    ) {
        TaskProcessor<CommutationDispatcherComponent> processor = new TaskProcessor<CommutationDispatcherComponent>(
                component,
                new RegularStartupExecutor(startupUnit)
        );
        lifecycleDependentComponentManager.addLifecycleObserver(processor);
        return processor;
    }
}
