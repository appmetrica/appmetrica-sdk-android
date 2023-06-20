package io.appmetrica.analytics.impl.component.processor.factory;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;
import java.util.List;

public class RegularMainReporterFactory extends RegularFactory {

    public RegularMainReporterFactory(ReportingHandlerProvider provider) {
        super(provider);
    }

    @Override
    public void addHandlers(@NonNull List<ReportComponentHandler> reportHandlers) {
        reportHandlers.add(getProvider().getReportPermissionsHandler());
        reportHandlers.add(getProvider().getReportFeaturesHandler());
        super.addHandlers(reportHandlers);
    }

}
