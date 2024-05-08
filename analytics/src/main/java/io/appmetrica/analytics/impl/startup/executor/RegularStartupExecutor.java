package io.appmetrica.analytics.impl.startup.executor;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.startup.StartupUnit;
import io.appmetrica.analytics.logger.internal.YLogger;
import io.appmetrica.analytics.networktasks.internal.NetworkTask;

public class RegularStartupExecutor implements StartupExecutor {

    private static final String TAG = "[RegularStartupExecutor]";

    private final StartupUnit startupUnit;

    public RegularStartupExecutor(@NonNull StartupUnit startupUnit) {
        this.startupUnit = startupUnit;
    }

    @Override
    public void sendStartupIfRequired() {
        YLogger.info(TAG, "sendStartupIfRequired");
        NetworkTask startupTask = startupUnit.getOrCreateStartupTaskIfRequired();
        if (startupTask != null) {
            GlobalServiceLocator.getInstance().getNetworkCore().startTask(startupTask);
        } else {
            YLogger.debug(TAG, "Not sending startup because startup task is null");
        }
    }

}
