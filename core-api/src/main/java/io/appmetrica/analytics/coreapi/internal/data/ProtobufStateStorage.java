package io.appmetrica.analytics.coreapi.internal.data;

import androidx.annotation.NonNull;

public interface ProtobufStateStorage<T> {

    public void save(@NonNull T state);

    @NonNull
    public T read();

    void delete();
}
