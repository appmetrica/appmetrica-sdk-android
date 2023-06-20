package io.appmetrica.analytics.coreutils.internal.io;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.io.Compressor;
import java.io.IOException;

public class GZIPCompressor implements Compressor {

    @Override
    @Nullable
    public byte[] compress(@Nullable final byte[] input) throws IOException {
        if (input == null) {
            return null;
        }
        return GZIPUtils.gzipBytes(input);
    }

    @Override
    @Nullable
    public byte[] uncompress(@Nullable final byte[] input) throws IOException {
        if (input == null) {
            return null;
        }
        return GZIPUtils.unGzipBytes(input);
    }
}
