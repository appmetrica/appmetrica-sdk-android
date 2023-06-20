package io.appmetrica.analytics.impl.component.processor.event;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;

public class SaveInitialUserProfileIDHandler extends ReportComponentHandler {

    private static final String TAG = "[SaveInitialUserProfileIDHandler]";

    public SaveInitialUserProfileIDHandler(@NonNull ComponentUnit component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull CounterReport reportData) {
        String userProfileID = reportData.getProfileID();
        if (!TextUtils.isEmpty(userProfileID)) {
            YLogger.info(TAG, "save initial userProfileID = %s", userProfileID);
            getComponent().setProfileID(userProfileID);
        }
        return false;
    }
}
