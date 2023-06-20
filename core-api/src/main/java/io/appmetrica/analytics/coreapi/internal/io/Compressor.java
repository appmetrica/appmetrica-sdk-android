package io.appmetrica.analytics.coreapi.internal.io;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.IOException;

public interface Compressor {

    @Nullable
    byte[] compress(@NonNull final byte[] input) throws IOException;

    @Nullable
    byte[] uncompress(@NonNull final byte[] input) throws IOException;
}
