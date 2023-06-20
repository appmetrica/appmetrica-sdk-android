package io.appmetrica.analytics.impl.component.processor.factory;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;
import java.util.List;

public class PurgeBufferFactory extends HandlersFactory<ReportComponentHandler> {

    public PurgeBufferFactory(ReportingHandlerProvider provider) {
        super(provider);
    }

    @Override
    public void addHandlers(@NonNull List<ReportComponentHandler> reportHandlers) {
        reportHandlers.add(getProvider().getReportPurgeBufferHandler());
    }
}
