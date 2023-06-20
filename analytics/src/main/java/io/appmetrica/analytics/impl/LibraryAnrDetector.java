package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import java.util.List;
import java.util.regex.Pattern;

public class LibraryAnrDetector {

    private static final Pattern APPMETRICA_NOT_PUSH_PATTERN =
        Pattern.compile("io\\.appmetrica\\.analytics\\.(?!push)");
    private static final Pattern PUSH_PATTERN = Pattern.compile("io\\.appmetrica\\.analytics\\.push\\..*");

    public boolean isAppmetricaAnr(@NonNull List<StackTraceElement> stacktrace) {
        return doesAnrMatchPattern(stacktrace, APPMETRICA_NOT_PUSH_PATTERN);
    }

    public boolean isPushAnr(@NonNull List<StackTraceElement> stacktrace) {
        return doesAnrMatchPattern(stacktrace, PUSH_PATTERN);
    }

    private boolean doesAnrMatchPattern(@NonNull List<StackTraceElement> stacktrace, @NonNull Pattern pattern) {
        for (StackTraceElement element : stacktrace) {
            if (pattern.matcher(element.getClassName()).find()) {
                return true;
            }
        }
        return false;
    }
}
