package io.appmetrica.analytics.impl.component.processor.factory;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;
import java.util.List;

public class ActivationFactory extends HandlersFactory<ReportComponentHandler> {

    public ActivationFactory(ReportingHandlerProvider provider) {
        super(provider);
    }

    @Override
    public void addHandlers(@NonNull List<ReportComponentHandler> reportHandlers) {
        reportHandlers.add(getProvider().getApplySettingsFromActivationConfigHandler());
        reportHandlers.add(getProvider().getSavePreloadInfoHandler());
        reportHandlers.add(getProvider().getSaveInitialUserProfileIDHandler());
        reportHandlers.add(getProvider().getReportFirstHandler());
        reportHandlers.add(getProvider().getSubscribeForReferrerHandler());
    }
}
