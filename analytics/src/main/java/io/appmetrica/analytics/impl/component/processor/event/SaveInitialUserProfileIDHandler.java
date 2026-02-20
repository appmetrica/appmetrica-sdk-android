package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class SaveInitialUserProfileIDHandler extends ReportComponentHandler {

    private static final String TAG = "[SaveInitialUserProfileIDHandler]";

    public SaveInitialUserProfileIDHandler(@NonNull ComponentUnit component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull CounterReport reportData) {
        String userProfileID = reportData.getProfileID();
        if (!StringUtils.isNullOrEmpty(userProfileID)) {
            DebugLogger.INSTANCE.info(TAG, "save initial userProfileID = %s", userProfileID);
            getComponent().setProfileID(userProfileID);
        }
        return false;
    }
}
