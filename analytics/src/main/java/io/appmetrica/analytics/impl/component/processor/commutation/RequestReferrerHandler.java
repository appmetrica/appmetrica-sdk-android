package io.appmetrica.analytics.impl.component.processor.commutation;

import android.os.Bundle;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.clients.CommutationClientUnit;
import io.appmetrica.analytics.impl.referrer.common.ReferrerResultReceiver;

public class RequestReferrerHandler extends CommutationHandler {

    private static final String TAG = "[RequestReferrerHandler]";

    public RequestReferrerHandler(final CommutationDispatcherComponent component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull CounterReport reportData, @NonNull CommutationClientUnit clientUnit) {
        Bundle payload = reportData.getPayload();
        YLogger.info(TAG, "process report with payload: %s", payload);
        ResultReceiver receiver = null;
        if (payload != null) {
            receiver = payload.getParcelable(ReferrerResultReceiver.BUNDLE_KEY);
        }
        getComponent().requestReferrer(receiver);
        return false;
    }
}
