package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;

public class SubscribeForReferrerHandler extends ReportComponentHandler {

    public SubscribeForReferrerHandler(@NonNull ComponentUnit component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull CounterReport reportData) {
        getComponent().subscribeForReferrer();
        return false;
    }
}
