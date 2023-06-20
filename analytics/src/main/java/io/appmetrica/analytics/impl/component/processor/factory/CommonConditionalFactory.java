package io.appmetrica.analytics.impl.component.processor.factory;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.EventsManager;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;
import java.util.List;

public class CommonConditionalFactory extends CommonHandlersFactory<ReportComponentHandler> {

    public CommonConditionalFactory(@NonNull ReportingHandlerProvider provider) {
        super(provider);
    }

    @Override
    public void addHandlers(@NonNull InternalEvents eventType,
                            @NonNull List<ReportComponentHandler> reportHandlers) {
        if (EventsManager.shouldApplyModuleHandlers(eventType)) {
            reportHandlers.add(getProvider().getModulesEventHandler());
        }
        if (affectsSessionState(eventType)) {
            reportHandlers.add(getProvider().getReportSessionHandler());
        }
    }

    private boolean affectsSessionState(@NonNull InternalEvents eventType) {
        return EventsManager.affectSessionState(eventType);
    }
}
