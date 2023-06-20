package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.utils.ServerTime;

public class ApplySettingsFromActivationConfigHandler extends ReportComponentHandler {

    private static final String TAG = "[ApplySettingsFromActivationConfigHandler]";

    @NonNull
    private final VitalComponentDataProvider vitalComponentDataProvider;
    @NonNull
    private final ServerTime mServerTime;

    public ApplySettingsFromActivationConfigHandler(@NonNull ComponentUnit component) {
        this(
                component,
                component.getVitalComponentDataProvider(),
                ServerTime.getInstance()
        );
    }

    @VisibleForTesting
    ApplySettingsFromActivationConfigHandler(@NonNull final ComponentUnit component,
                                             @NonNull final VitalComponentDataProvider vitalComponentDataProvider,
                                             @NonNull final ServerTime serverTime) {
        super(component);
        this.vitalComponentDataProvider = vitalComponentDataProvider;
        mServerTime = serverTime;
    }

    @Override
    public boolean process(@NonNull CounterReport reportData) {
        ComponentUnit component = getComponent();
        if (!vitalComponentDataProvider.isFirstEventDone() && !vitalComponentDataProvider.isInitEventDone()) {
            YLogger.info(TAG, "For componentId = %s: first and init events do not exist", component.getComponentId());
            if (component.getFreshReportRequestConfig().isFirstActivationAsUpdate()) {
                mServerTime.disableTimeDifferenceChecking();
            }
            getComponent().getEventFirstOccurrenceService().reset();
        }
        return false;
    }
}
