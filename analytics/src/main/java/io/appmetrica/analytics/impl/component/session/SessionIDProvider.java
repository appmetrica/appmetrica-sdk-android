package io.appmetrica.analytics.impl.component.session;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;

public class SessionIDProvider {

    static final long SESSION_ID_MIN_LIMIT = 10000000000L; //10 000 000 000 = 10^10

    @NonNull
    private final VitalComponentDataProvider vitalComponentDataProvider;

    public SessionIDProvider(@NonNull VitalComponentDataProvider vitalComponentDataProvider) {
        this.vitalComponentDataProvider = vitalComponentDataProvider;
    }

    public long getNextSessionId() {
        long previousSessionId = vitalComponentDataProvider.getSessionId();
        long newSessionId = previousSessionId < SESSION_ID_MIN_LIMIT ? SESSION_ID_MIN_LIMIT : previousSessionId + 1;
        vitalComponentDataProvider.setSessionId(newSessionId);
        return newSessionId;
    }

}
