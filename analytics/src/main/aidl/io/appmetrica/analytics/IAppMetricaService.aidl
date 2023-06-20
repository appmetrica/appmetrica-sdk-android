package io.appmetrica.analytics;

import android.os.Bundle;

// When updating AIDL protocol, update SelfProcessReporter correspondingly
interface IAppMetricaService {
    oneway void resumeUserSession(in Bundle data);
    oneway void pauseUserSession(in Bundle data);
    oneway void reportData(int type, in Bundle data);
}
