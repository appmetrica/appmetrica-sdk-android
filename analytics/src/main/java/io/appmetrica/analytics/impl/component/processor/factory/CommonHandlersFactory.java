package io.appmetrica.analytics.impl.component.processor.factory;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.InternalEvents;
import java.util.List;

public abstract class CommonHandlersFactory<BaseHandler> {

    private final ReportingHandlerProvider mProvider;

    public CommonHandlersFactory(ReportingHandlerProvider provider) {
        mProvider = provider;
    }

    public abstract void addHandlers(InternalEvents eventType, @NonNull List<BaseHandler> reportHandlers);

    public ReportingHandlerProvider getProvider() {
        return mProvider;
    }
}
