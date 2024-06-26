package io.appmetrica.analytics.impl.utils.limitation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;

public class StringTrimmer extends BaseTrimmer<String> {

    public StringTrimmer(int maxSize, @NonNull String logName) {
        this(maxSize, logName, PublicLogger.getAnonymousInstance());
    }

    public StringTrimmer(int maxSize, @NonNull String logName, @NonNull PublicLogger logger) {
        super(maxSize, logName, logger);
    }

    @Nullable
    @Override
    public String trim(@Nullable String data) {
        if (data != null && data.length() > getMaxSize()) {
            String newValue = data.substring(0, getMaxSize());
            mPublicLogger.warning(
                "\"%s\" %s size exceeded limit of %d characters",
                getLogName(),
                data,
                getMaxSize()
            );
            return newValue;
        } else {
            return data;
        }
    }
}
