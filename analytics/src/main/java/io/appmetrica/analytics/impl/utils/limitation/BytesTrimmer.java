package io.appmetrica.analytics.impl.utils.limitation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.utils.PublicLogger;

public class BytesTrimmer extends BaseTrimmer<byte[]> {

    public BytesTrimmer(int maxSize, @NonNull String logName, @NonNull PublicLogger publicLogger) {
        super(maxSize, logName, publicLogger);
    }

    @Nullable
    @Override
    public byte[] trim(@Nullable byte[] data) {
        byte [] result = data;
        if (data != null && data.length > getMaxSize()) {
            result = new byte[getMaxSize()];
            System.arraycopy(data, 0, result, 0, getMaxSize());
            if (mPublicLogger.isEnabled()) {
                mPublicLogger.fw(
                        "\"%s\" %s exceeded limit of %d bytes",
                        getLogName(),
                        data,
                        getMaxSize()
                );
            }
        }
        return result;
    }

}
