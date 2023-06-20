package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.db.preferences.EventNumberOfTypeItemsHolder;

public class EventNumberGenerator {

    @NonNull
    private final EventNumberOfTypeItemsHolder eventNumberOfTypeItemsHolder;
    @NonNull
    private final VitalComponentDataProvider vitalComponentDataProvider;

    public EventNumberGenerator(@NonNull VitalComponentDataProvider vitalComponentDataProvider) {
        this(vitalComponentDataProvider, new EventNumberOfTypeItemsHolder(vitalComponentDataProvider));
    }

    @VisibleForTesting
    EventNumberGenerator(@NonNull VitalComponentDataProvider vitalComponentDataProvider,
                         @NonNull EventNumberOfTypeItemsHolder eventNumberOfTypeItemsHolder) {
        this.vitalComponentDataProvider = vitalComponentDataProvider;
        this.eventNumberOfTypeItemsHolder = eventNumberOfTypeItemsHolder;
    }

    public long getEventGlobalNumberAndGenerateNext() {
        long number = vitalComponentDataProvider.getGlobalNumber();
        vitalComponentDataProvider.setGlobalNumber(number + 1);
        return number;
    }

    public long getEventNumberOfTypeAndGenerateNext(final int type) {
        long number = eventNumberOfTypeItemsHolder.getNumberOfType(type);
        eventNumberOfTypeItemsHolder.putNumberOfType(type, number + 1);
        return number;
    }
}
