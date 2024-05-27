package io.appmetrica.analytics.logger.common.impl;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Arrays;
import java.util.Locale;

public class LogMessageConstructor {

    private static final String TAG = "[LogMessageConstructor]";

    public String construct(@NonNull String tag, @Nullable String message, @Nullable Object... args) {
        return tag + " " + prepareMessage(message, args);
    }

    public String construct(
        @NonNull String tag,
        @Nullable Throwable throwable,
        @Nullable String message,
        @Nullable Object... args
    ) {
        final String nonNullMessage = (message == null ? "" : message) + "\n" + Log.getStackTraceString(throwable);
        return construct(tag, nonNullMessage, args);
    }

    private String prepareMessage(@Nullable String message, @Nullable Object... args) {
        String resultString;
        if (message == null) {
            resultString = "";
        } else if (args == null || args.length == 0) {
            resultString = message;
        } else {
            try {
                resultString = String.format(Locale.US, message, args);
            } catch (Throwable e) {
                resultString = "Attention!!! Invalid log format. See exception details above. Message: " +
                    message + "; arguments: " + Arrays.toString(args);
                Log.e(TAG, resultString, e);
            }
        }
        return String.format(
            Locale.US,
            "[%d-%s] %s",
            Thread.currentThread().getId(),
            Thread.currentThread().getName(),
            resultString
        );
    }
}
