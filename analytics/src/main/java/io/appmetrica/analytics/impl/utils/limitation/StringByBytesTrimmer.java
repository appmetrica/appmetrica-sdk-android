package io.appmetrica.analytics.impl.utils.limitation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.IOUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import java.io.UnsupportedEncodingException;

public class StringByBytesTrimmer extends BaseTrimmer<String> {

    private static final String TAG = "[StringByBytesTrimmer]";

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
            try {
                byte[] bytes = data.getBytes(IOUtils.UTF8_ENCODING);
                if (bytes.length > getMaxSize()) {
                    result = new String(bytes, 0, getMaxSize(), IOUtils.UTF8_ENCODING);
                    mPublicLogger.warning(
                        "\"%s\" %s exceeded limit of %d bytes",
                        getLogName(),
                        data,
                        getMaxSize()
                    );
                }
            } catch (UnsupportedEncodingException e) {
                DebugLogger.INSTANCE.error(TAG, e, "error while cutting string for %s", getLogName());
            }
        }
        return result;
    }
}
