package io.appmetrica.analytics.impl.component.processor.commutation;

import android.os.Bundle;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.ServiceEvent;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.clients.CommutationClientUnit;
import io.appmetrica.analytics.impl.referrer.common.ReferrerResultReceiver;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class RequestReferrerHandler extends CommutationHandler {

    private static final String TAG = "[RequestReferrerHandler]";

    public RequestReferrerHandler(final CommutationDispatcherComponent component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull ServiceEvent serviceEvent, @NonNull CommutationClientUnit clientUnit) {
        Bundle payload = serviceEvent.getPayload();
        DebugLogger.INSTANCE.info(TAG, "process report with payload: %s", payload);
        ResultReceiver receiver = null;
        if (payload != null) {
            receiver = payload.getParcelable(ReferrerResultReceiver.BUNDLE_KEY);
        }
        getComponent().requestReferrer(receiver);
        return false;
    }
}
