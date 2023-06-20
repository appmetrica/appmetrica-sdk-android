package io.appmetrica.analytics.impl.utils.limitation;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.IOUtils;
import io.appmetrica.analytics.impl.utils.LoggerStorage;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import java.io.UnsupportedEncodingException;

public class StringByBytesTrimmer extends BaseTrimmer<String> {

    public StringByBytesTrimmer(int maxSize, @NonNull String tag) {
        this(maxSize, tag, LoggerStorage.getAnonymousPublicLogger());
    }

    public StringByBytesTrimmer(int maxSize, @NonNull String tag, @NonNull PublicLogger logger) {
        super(maxSize, tag, logger);
    }

    @Nullable
    @Override
    public String trim(@Nullable String data) {
        String result = data;
        if (!TextUtils.isEmpty(data)) {
            try {
                byte[] bytes = data.getBytes(IOUtils.UTF8_ENCODING);
                if (bytes.length > getMaxSize()) {
                    result = new String(bytes, 0, getMaxSize(), IOUtils.UTF8_ENCODING);
                    if (mPublicLogger.isEnabled()) {
                        mPublicLogger.fw(
                                "\"%s\" %s exceeded limit of %d bytes",
                                getLogName(),
                                data,
                                getMaxSize()
                        );
                    }
                }
            } catch (UnsupportedEncodingException e) {
                YLogger.e(e, "error while cutting string for %s", getLogName());
            }
        }
        return result;
    }
}
