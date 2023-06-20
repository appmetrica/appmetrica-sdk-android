package io.appmetrica.analytics.impl.component.processor.event;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.ClientCounterReport;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;

public class UpdateUserProfileIDHandler extends ReportComponentHandler {

    public UpdateUserProfileIDHandler(ComponentUnit component) {
        super(component);
    }

    @Override
    public boolean process(@NonNull CounterReport reportData) {
        String oldProfileID = getComponent().getProfileID();
        String newProfileId = reportData.getProfileID();
        getComponent().setProfileID(newProfileId);
        if (TextUtils.equals(oldProfileID, newProfileId) == false) {
            getComponent().handleReport(ClientCounterReport.formUserProfileEvent());
        }
        return false;
    }

}
