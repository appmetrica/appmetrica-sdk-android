package io.appmetrica.analytics.impl.preparer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface EventTypeComposer {

    @Nullable
    Integer getEventType(@NonNull EventFromDbModel event);
}
