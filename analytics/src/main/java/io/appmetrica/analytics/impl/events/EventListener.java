package io.appmetrica.analytics.impl.events;

import androidx.annotation.NonNull;
import java.util.List;

public interface EventListener {

    void onEventsAdded(@NonNull List<Integer> reportTypes);

    void onEventsRemoved(@NonNull List<Integer> reportTypes);
}
