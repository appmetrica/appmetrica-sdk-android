package io.appmetrica.analytics.coreapi.internal.data;

import androidx.annotation.NonNull;
import java.io.IOException;

public interface StateSerializer<T> {

    @NonNull
    byte[] toByteArray(@NonNull T message);

    @NonNull
    T toState(@NonNull byte[] data) throws IOException;

    @NonNull
    T defaultValue();

}
