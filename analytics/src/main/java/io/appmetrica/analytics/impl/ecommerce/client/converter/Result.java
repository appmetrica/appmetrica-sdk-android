package io.appmetrica.analytics.impl.ecommerce.client.converter;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;

public class Result<R, M extends BytesTruncatedProvider> implements BytesTruncatedProvider {
    @NonNull
    public final R result;
    @NonNull
    public final M metaInfo;

    public Result(@NonNull R result, @NonNull M metaInfo) {
        this.result = result;
        this.metaInfo = metaInfo;
    }

    @Override
    public int getBytesTruncated() {
        return metaInfo.getBytesTruncated();
    }

    @NonNull
    @Override
    public String toString() {
        return "Result{" +
                "result=" + result +
                ", metaInfo=" + metaInfo +
                '}';
    }
}
