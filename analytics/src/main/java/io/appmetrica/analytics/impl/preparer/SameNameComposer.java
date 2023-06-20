package io.appmetrica.analytics.impl.preparer;

import androidx.annotation.Nullable;

public class SameNameComposer implements NameComposer {

    @Nullable
    @Override
    public String getName(@Nullable String originalName) {
        return originalName;
    }
}
