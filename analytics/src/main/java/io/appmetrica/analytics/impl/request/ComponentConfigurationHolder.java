package io.appmetrica.analytics.impl.request;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.networktasks.internal.BaseRequestConfig;

public abstract class ComponentConfigurationHolder<
            T extends CoreRequestConfig,
            A extends BaseRequestConfig.BaseRequestArguments<CommonArguments.ReporterArguments, A>,
            L extends BaseRequestConfig.ComponentLoader<T, A, CoreRequestConfig.CoreDataSource<A>>
        > extends ConfigurationHolder<T, CommonArguments.ReporterArguments, A, L> {

    public ComponentConfigurationHolder(@NonNull L loader,
                                        @NonNull StartupState startupState,
                                        @NonNull A initialArguments) {
        super(loader, startupState, initialArguments);
    }

    public synchronized void updateArguments(@NonNull CommonArguments.ReporterArguments configuration) {
        super.updateArguments(configuration);
    }

}
