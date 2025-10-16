package io.appmetrica.analytics.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceComponentsInitializer;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.impl.modules.ModuleStatus;
import io.appmetrica.analytics.impl.modules.ModuleStatusReporter;
import io.appmetrica.analytics.impl.modules.ModulesSeeker;
import io.appmetrica.analytics.impl.service.migration.ServiceMigrationManager;
import io.appmetrica.analytics.impl.servicecomponents.ServiceComponentsInitializerProvider;
import java.util.List;

public class FirstServiceEntryPointManager {

    public static final FirstServiceEntryPointManager INSTANCE = new FirstServiceEntryPointManager();

    @NonNull
    private final ModulesSeeker modulesSeeker = new ModulesSeeker();

    @NonNull
    private final ServiceComponentsInitializer serviceComponentsInitializer =
        new ServiceComponentsInitializerProvider().getServiceComponentsInitializer();

    private boolean initialized = false;

    public void onPossibleFirstEntry(@NonNull Context context) {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    init(context);
                    initialized = true;
                }
            }
        }
    }

    private void init(@NonNull Context context) {
        GlobalServiceLocator.init(context);
        serviceComponentsInitializer.onCreate(context);
        List<ModuleStatus> result = modulesSeeker.discoverServiceModules();
        new ServiceMigrationManager().checkMigration(context);
        GlobalServiceLocator.getInstance().getMultiProcessSafeUuidProvider().readUuid();
        createModuleStatusReporter().reportModulesStatus(result);
    }
    
    private ModuleStatusReporter createModuleStatusReporter() {
        return new ModuleStatusReporter(
            GlobalServiceLocator.getInstance().getServiceExecutorProvider().getDefaultExecutor(),
            GlobalServiceLocator.getInstance().getServicePreferences(),
            "service_modules",
            new SystemTimeProvider()
        );
    }

    @VisibleForTesting
    FirstServiceEntryPointManager() {
    }

    @VisibleForTesting
    void reset() {
        initialized = false;
    }
}
