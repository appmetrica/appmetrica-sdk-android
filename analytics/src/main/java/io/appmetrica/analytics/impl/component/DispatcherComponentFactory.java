package io.appmetrica.analytics.impl.component;

import android.content.Context;
import androidx.annotation.NonNull;

public interface DispatcherComponentFactory<T extends IDispatcherComponent> {

    T createDispatcherComponent(@NonNull Context context,
                                @NonNull ComponentId componentId,
                                @NonNull CommonArguments arguments);
}
