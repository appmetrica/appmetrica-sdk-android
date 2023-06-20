package io.appmetrica.analytics.impl.component;

import android.content.Context;
import androidx.annotation.NonNull;

public interface IBaseComponent {

    @NonNull
    Context getContext();

    @NonNull
    ComponentId getComponentId();

}
