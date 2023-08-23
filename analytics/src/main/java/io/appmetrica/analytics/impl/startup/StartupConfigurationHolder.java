 package io.appmetrica.analytics.impl.startup;

 import androidx.annotation.NonNull;
 import io.appmetrica.analytics.impl.request.ConfigurationHolder;
 import io.appmetrica.analytics.impl.request.CoreRequestConfig;
 import io.appmetrica.analytics.impl.request.StartupRequestConfig;

public class StartupConfigurationHolder extends
        ConfigurationHolder<StartupRequestConfig,
                StartupRequestConfig.Arguments,
                StartupRequestConfig.Arguments,
                StartupRequestConfig.Loader> {

    StartupConfigurationHolder(@NonNull StartupRequestConfig.Loader loader,
                               @NonNull StartupState startupState,
                               @NonNull StartupRequestConfig.Arguments initialArguments) {
        super(loader, startupState, initialArguments);
    }

    //This operation is not so frequent. And comparing two Arguments more complex, then new StartupRequestConfig loading
    public synchronized void updateArguments(@NonNull StartupRequestConfig.Arguments newArguments) {
        setDataSource(new CoreRequestConfig.CoreDataSource<>(
                getStartupState(), getArguments().mergeFrom(newArguments)
        ));
        reset();
    }
}
