package io.appmetrica.analytics.impl.component.processor.commutation;

import android.os.Bundle;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.IdentifiersData;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.clients.CommutationClientUnit;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class ForceStartupHandler extends CommutationHandler {

    private static final String TAG = "[ForceStartupHandler]";

    public ForceStartupHandler(CommutationDispatcherComponent component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull CounterReport reportData, @NonNull CommutationClientUnit clientUnit) {
        DebugLogger.INSTANCE.info(TAG, "process: %s", reportData);
        Bundle payload = reportData.getPayload();
        IdentifiersData identifiersData = null;
        if (payload != null) {
            identifiersData = payload.getParcelable(IdentifiersData.BUNDLE_KEY);
        }
        getComponent().provokeStartupOrGetCurrentState(identifiersData);
        return false;
    }

}
