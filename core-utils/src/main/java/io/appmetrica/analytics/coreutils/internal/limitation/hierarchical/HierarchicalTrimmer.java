package io.appmetrica.analytics.coreutils.internal.limitation.hierarchical;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.coreutils.internal.limitation.TrimmingResult;

public interface HierarchicalTrimmer<V, M extends BytesTruncatedProvider> {

    @NonNull
    TrimmingResult<V, M> trim(@Nullable V input);
}
