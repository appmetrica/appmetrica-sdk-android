package io.appmetrica.analytics.impl.component.processor.factory;

import androidx.annotation.NonNull;
import java.util.List;

public abstract class HandlersFactory<BaseHandler> {

    private final ReportingHandlerProvider mProvider;

    public HandlersFactory(@NonNull ReportingHandlerProvider provider) {
        mProvider = provider;
    }

    public abstract void addHandlers(@NonNull List<BaseHandler> reportHandlers);

    public ReportingHandlerProvider getProvider() {
        return mProvider;
    }
}
