package io.appmetrica.analytics.coreutils.internal.limitation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import java.nio.charset.StandardCharsets;

public class StringByBytesTrimmer extends BaseTrimmer<String> {

    public StringByBytesTrimmer(int maxSize, @NonNull String tag) {
        this(maxSize, tag, PublicLogger.getAnonymousInstance());
    }

    public StringByBytesTrimmer(int maxSize, @NonNull String tag, @NonNull PublicLogger logger) {
        super(maxSize, tag, logger);
    }

    @Nullable
    @Override
    public String trim(@Nullable String data) {
        String result = data;
        if (!StringUtils.isNullOrEmpty(data)) {
            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            if (bytes.length > getMaxSize()) {
                result = new String(bytes, 0, getMaxSize(), StandardCharsets.UTF_8);
                mPublicLogger.warning(
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
