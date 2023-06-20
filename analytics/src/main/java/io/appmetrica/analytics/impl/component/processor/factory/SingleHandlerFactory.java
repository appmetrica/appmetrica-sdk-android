package io.appmetrica.analytics.impl.component.processor.factory;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;
import java.util.List;

public class SingleHandlerFactory extends HandlersFactory<ReportComponentHandler> {

    private final ReportComponentHandler mHandler;

    public SingleHandlerFactory(ReportingHandlerProvider provider, ReportComponentHandler handler) {
        super(provider);
        mHandler = handler;
    }

    @Override
    public void addHandlers(@NonNull List<ReportComponentHandler> reportHandlers) {
        reportHandlers.add(mHandler);
    }
}
