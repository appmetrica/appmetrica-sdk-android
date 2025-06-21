package io.appmetrica.analytics.impl.component.processor.factory;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;
import java.util.List;

public class UnhandledExceptionFactory extends HandlersFactory<ReportComponentHandler> {

    public UnhandledExceptionFactory(ReportingHandlerProvider provider) {
        super(provider);
    }

    @Override
    public void addHandlers(@NonNull List<ReportComponentHandler> reportHandlers) {
        reportHandlers.add(getProvider().getReportPurgeBufferHandler());
        reportHandlers.add(getProvider().getReportSaveToDatabaseHandler());
        reportHandlers.add(getProvider().getReportCrashMetaInformation());
        reportHandlers.add(getProvider().getReportSessionStopHandler());
    }
}
