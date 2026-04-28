package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.ClientCounterReport;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class UpdateUserProfileIDHandler extends ReportComponentHandler {

    private static final String TAG = "[UpdateUserProfileIDHandler]";

    public UpdateUserProfileIDHandler(ComponentUnit component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull CounterReport reportData) {
        String oldProfileID = getComponent().getProfileID();
        String newProfileId = reportData.getProfileID();
        getComponent().setProfileID(newProfileId);
        if (!StringUtils.equalsNullSafety(oldProfileID, newProfileId)) {
            DebugLogger.INSTANCE.info(TAG, "update userProfileID from %s to %s", oldProfileID, newProfileId);
            getComponent().handleReport(ClientCounterReport.formUserProfileEvent());
        }
        return false;
    }

}
