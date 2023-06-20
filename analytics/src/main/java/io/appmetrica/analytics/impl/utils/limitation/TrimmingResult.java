package io.appmetrica.analytics.impl.utils.limitation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TrimmingResult<V, M extends BytesTruncatedProvider> implements BytesTruncatedProvider {
    @Nullable
    public final V value;
    @NonNull
    public final M metaInfo;

    public TrimmingResult(@Nullable V value, @NonNull M metaInfo) {
        this.value = value;
        this.metaInfo = metaInfo;
    }

    @Override
    public int getBytesTruncated() {
        return metaInfo.getBytesTruncated();
    }

    @NonNull
    @Override
    public String toString() {
        return "TrimmingResult{" +
                "value=" + value +
                ", metaInfo=" + metaInfo +
                '}';
    }
}
