package io.appmetrica.analytics.impl.preparer;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.StringUtils;

public class EmptyNameComposer implements NameComposer {

    @Nullable
    @Override
    public String getName(@Nullable String originalName) {
        return StringUtils.EMPTY;
    }
}
