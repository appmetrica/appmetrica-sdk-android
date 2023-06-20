package io.appmetrica.analytics.coreutils.internal.logger;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import java.util.Arrays;
import java.util.List;

class LogMessageByLineBreakSplitter implements ILogMessageSplitter {

    static final String DEFAULT_REGEX = "\\n";

    @NonNull
    private final String regex;

    public LogMessageByLineBreakSplitter() {
        this(DEFAULT_REGEX);
    }

    @Override
    public List<String> split(@NonNull String input) {
        return Arrays.asList(input.split(regex));
    }

    @VisibleForTesting
    LogMessageByLineBreakSplitter(@NonNull String regex) {
        this.regex = regex;
    }

}
