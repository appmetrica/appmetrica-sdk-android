package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;

public interface ClientIdentifiersChangedListener {

    void onClientIdentifiersChanged(@NonNull ClientIdentifiersHolder clientIdentifiersHolder);
}
