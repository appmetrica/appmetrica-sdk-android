package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.logger.internal.YLogger;

public class ReportFirstHandler extends ReportComponentHandler {

    private static final String TAG = "[ReportFirstHandler]";

    @NonNull
    private final VitalComponentDataProvider vitalComponentDataProvider;

    public ReportFirstHandler(@NonNull final ComponentUnit component) {
        this(component, component.getVitalComponentDataProvider());
    }

    @VisibleForTesting
    ReportFirstHandler(@NonNull final ComponentUnit component,
                       @NonNull VitalComponentDataProvider vitalComponentDataProvider) {
        super(component);
        this.vitalComponentDataProvider = vitalComponentDataProvider;
    }

    @Override
    public boolean process(@NonNull final CounterReport reportData) {
        ComponentUnit component = getComponent();
        if (!vitalComponentDataProvider.isFirstEventDone()) {
            YLogger.info(TAG, "For componentId = %s: first event does not exist", component.getComponentId());
            if (!vitalComponentDataProvider.isInitEventDone()) {
                YLogger.info(TAG, "For componentId = %s: init event does not exist", component.getComponentId());
                component.getEventSaver().identifyAndSaveFirstEventReport(
                        CounterReport.formFirstEventReportData(reportData)
                );
            }
            vitalComponentDataProvider.setFirstEventDone(true);
        }
        return false;
    }
}
