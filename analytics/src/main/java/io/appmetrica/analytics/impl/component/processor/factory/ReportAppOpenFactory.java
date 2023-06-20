package io.appmetrica.analytics.impl.component.processor.factory;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;
import java.util.List;

public class ReportAppOpenFactory extends HandlersFactory<ReportComponentHandler>{

    public ReportAppOpenFactory(final ReportingHandlerProvider provider) {
        super(provider);
    }

    @Override
    public void addHandlers(@NonNull final List<ReportComponentHandler> reportHandlers) {
        reportHandlers.add(getProvider().getReportAppOpenHandler());
        reportHandlers.add(getProvider().getReportSaveToDatabaseHandler());
    }
}
