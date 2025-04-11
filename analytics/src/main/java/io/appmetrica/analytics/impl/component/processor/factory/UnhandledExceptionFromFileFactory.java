package io.appmetrica.analytics.impl.component.processor.factory;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;
import java.util.List;

public class UnhandledExceptionFromFileFactory extends HandlersFactory<ReportComponentHandler> {

    public UnhandledExceptionFromFileFactory(ReportingHandlerProvider provider) {
        super(provider);
    }

    @Override
    public void addHandlers(@NonNull List<ReportComponentHandler> reportHandlers) {
        reportHandlers.add(getProvider().getReportPrevSessionEventHandler());
        reportHandlers.add(getProvider().getReportCrashMetaInformation());
    }
}
