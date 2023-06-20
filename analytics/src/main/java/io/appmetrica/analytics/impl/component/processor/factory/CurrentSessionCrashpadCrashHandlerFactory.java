package io.appmetrica.analytics.impl.component.processor.factory;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;
import java.util.List;

public class CurrentSessionCrashpadCrashHandlerFactory extends HandlersFactory<ReportComponentHandler> {

    public CurrentSessionCrashpadCrashHandlerFactory(ReportingHandlerProvider provider) {
        super(provider);
    }

    @Override
    public void addHandlers(@NonNull List<ReportComponentHandler> reportHandlers) {
        reportHandlers.add(getProvider().getReportSaveToDatabaseHandler());
        reportHandlers.add(getProvider().getReportPurgeBufferHandler());
        reportHandlers.add(getProvider().getReportSessionStopHandler());
    }
}
