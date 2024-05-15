package io.appmetrica.analytics.logger.impl;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Arrays;
import java.util.Locale;
import org.json.JSONObject;

public class LogMessageConstructor {

    private static final int JSON_INDENT_SPACES = 2;
    private static final String DUMP_EXCEPTION_MESSAGE = "Exception during dumping JSONObject";

    private static final String TAG = "[LogMessageConstructor]";

    public String construct(@NonNull String tag, @NonNull String message, Object... args) {
        return tag + " " + prepareMessage(message, args);
    }

    public String construct(@NonNull String tag, @Nullable Throwable e, @Nullable String message, Object... args) {
        final String nonNullMessage = (message == null ? "" : message) + "\n" + Log.getStackTraceString(e);
        return construct(tag, nonNullMessage, args);
    }

    public String construct(@NonNull String tag, @NonNull JSONObject jsonObject) {
        try {
            return tag + "\n" + jsonObject.toString(JSON_INDENT_SPACES);
        } catch (Throwable e) {
            Log.e(TAG, DUMP_EXCEPTION_MESSAGE, e);
        }
        return tag + "\n" + DUMP_EXCEPTION_MESSAGE;
    }

    private String prepareMessage(@NonNull String message, Object... args) {
        String resultString = "Attention!!! Invalid log format. See exception details above. Message: " +
            message + "; arguments: " + Arrays.toString(args);
        try {
            resultString = (args == null || args.length == 0) ? message : String.format(Locale.US, message, args);
        } catch (Throwable e) {
            Log.e(TAG, resultString, e);
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
