package io.appmetrica.analytics.impl.component.processor.factory;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;
import java.util.List;

public class ActivationFactory extends HandlersFactory<ReportComponentHandler> {

    private final ComponentId componentId;

    public ActivationFactory(ReportingHandlerProvider provider, ComponentId componentId) {
        super(provider);
        this.componentId = componentId;
    }

    @Override
    public void addHandlers(@NonNull List<ReportComponentHandler> reportHandlers) {
        reportHandlers.add(getProvider().getApplySettingsFromActivationConfigHandler());
        reportHandlers.add(getProvider().getSavePreloadInfoHandler());
        reportHandlers.add(getProvider().getSaveInitialUserProfileIDHandler());
        reportHandlers.add(getProvider().getReportFirstHandler());
        if (componentId.isMain()) {
            reportHandlers.add(getProvider().getSendReferrerEventHandler());
        }
    }
}
