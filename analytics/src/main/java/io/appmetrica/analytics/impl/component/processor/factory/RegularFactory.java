package io.appmetrica.analytics.impl.component.processor.factory;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;
import java.util.List;

public class RegularFactory extends HandlersFactory<ReportComponentHandler> {

    public RegularFactory(ReportingHandlerProvider provider) {
        super(provider);
    }

    @Override
    public void addHandlers(@NonNull List<ReportComponentHandler> reportHandlers) {
        reportHandlers.add(getProvider().getReportFirstOccurrenceStatusHandler());
        reportHandlers.add(getProvider().getReportSaveToDatabaseHandler());
    }

}
