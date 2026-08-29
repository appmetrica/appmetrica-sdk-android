package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.IReporter;
import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.crash.MetaInformation;
import java.util.HashMap;

public class ReportCrashMetaInformation extends ReportComponentHandler {

    @NonNull
    private final IReporter reporter;

    public ReportCrashMetaInformation(@NonNull ComponentUnit component,
                                      @NonNull IReporter reporter) {
        super(component);
        this.reporter = reporter;
    }

    @Override
    public boolean process(@NonNull CoreServiceEvent serviceEvent) {
        final MetaInformation metaInformation = MetaInformation.getMetaInformation(serviceEvent.getType());
        HashMap<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("type", metaInformation.type);
        arguments.put("delivery_method", metaInformation.deliveryMethod);
        reporter.reportEvent("crash_saved", arguments);
        return false;
    }

}
