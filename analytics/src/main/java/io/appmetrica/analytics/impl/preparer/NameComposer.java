package io.appmetrica.analytics.impl.preparer;

import androidx.annotation.Nullable;

public interface NameComposer {

    @Nullable
    String getName(@Nullable String originalName);
}
