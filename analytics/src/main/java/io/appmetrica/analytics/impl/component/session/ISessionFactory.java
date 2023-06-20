package io.appmetrica.analytics.impl.component.session;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ISessionFactory<A> {

    @Nullable
    Session load();

    @NonNull
    Session create(@NonNull A arguments);
}
