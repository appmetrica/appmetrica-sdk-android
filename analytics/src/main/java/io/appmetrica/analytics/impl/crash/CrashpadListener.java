package io.appmetrica.analytics.impl.crash;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.impl.ServiceLifecycleObserver;

public interface CrashpadListener extends ServiceLifecycleObserver {

    void addListener(@NonNull Consumer<String> listener);

    void removeListener(@NonNull Consumer<String> listener);
}
