package io.appmetrica.analytics.impl.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.logger.BaseLogger;
import io.appmetrica.analytics.impl.Utils;
import java.util.Locale;

public abstract class LoggerWithApiKey extends BaseLogger {

    private static String sLogPrefix = StringUtils.EMPTY;
    @NonNull private final String mPartialApiKey;

    public LoggerWithApiKey(@Nullable String fullApiKey) {
        super(false);
        mPartialApiKey = "[" + Utils.createPartialApiKey(fullApiKey) + "] ";
    }

    @NonNull
    @Override
    public String getPrefix() {
        String part1 = StringUtils.ifIsNullToDef(sLogPrefix, StringUtils.EMPTY);
        String part2 = StringUtils.ifIsNullToDef(mPartialApiKey, StringUtils.EMPTY);
        return part1 + part2;
    }

    public static void init(final Context context) {
        sLogPrefix = "[" + context.getPackageName() + "] : ";
    }

    @VisibleForTesting
    static void reset() {
        sLogPrefix = StringUtils.EMPTY;
    }

    @Override
    protected String formatMessage(final String message, final Object[] params) {
        return String.format(Locale.US, message, params);
    }
}
