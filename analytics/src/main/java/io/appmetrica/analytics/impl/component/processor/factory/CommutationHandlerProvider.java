package io.appmetrica.analytics.impl.component.processor.factory;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.processor.commutation.CommutationHandler;
import io.appmetrica.analytics.impl.component.processor.commutation.ForceStartupHandler;
import io.appmetrica.analytics.impl.component.processor.commutation.RequestReferrerHandler;
import io.appmetrica.analytics.impl.component.processor.commutation.UpdatePreActivationConfigHandler;

public class CommutationHandlerProvider {

    private final ForceStartupHandler mForceStartupHandler;
    @NonNull
    private final RequestReferrerHandler requestReferrerHandler;
    private final UpdatePreActivationConfigHandler mPreActivationConfigHandler;

    public CommutationHandlerProvider(CommutationDispatcherComponent component) {
        mForceStartupHandler = new ForceStartupHandler(component);
        requestReferrerHandler = new RequestReferrerHandler(component);
        mPreActivationConfigHandler = new UpdatePreActivationConfigHandler(
                component,
                GlobalServiceLocator.getInstance().getDataSendingRestrictionController()
        );
    }

    public ForceStartupHandler getForceStartupHandler() {
        return mForceStartupHandler;
    }

    @NonNull
    public RequestReferrerHandler getRequestReferrerHandler() {
        return requestReferrerHandler;
    }

    public CommutationHandler getUpdatePreActivationConfig() {
        return mPreActivationConfigHandler;
    }
}
