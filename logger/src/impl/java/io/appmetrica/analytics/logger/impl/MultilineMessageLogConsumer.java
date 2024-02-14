package io.appmetrica.analytics.logger.impl;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.Arrays;
import java.util.Locale;

class MultilineMessageLogConsumer implements IMessageLogConsumer<String> {

    private static final String TAG = "[MultilineMessageLogConsumer]";

    @NonNull
    private final IMessageLogConsumer<String> singleLineLogConsumer;
    @NonNull
    private final ILogMessageSplitter logMessageSplitter;

    public MultilineMessageLogConsumer(@NonNull IMessageLogConsumer<String> singleLineLogConsumer,
                                       @NonNull ILogMessageSplitter logMessageSplitter) {
        this.singleLineLogConsumer = singleLineLogConsumer;
        this.logMessageSplitter = logMessageSplitter;
    }

    @Override
    public void consume(@NonNull String message, Object... args) {
        for (String singleLineMessage : logMessageSplitter.split(prepareMessage(message, args))) {
            singleLineLogConsumer.consume(singleLineMessage);
        }
    }

    @Override
    public void consumeWithTag(@NonNull String tag, @NonNull String message, Object... args) {
        for (String singleLineMessage : logMessageSplitter.split(prepareMessage(message, args))) {
            singleLineLogConsumer.consumeWithTag(tag, singleLineMessage);
        }
    }

    private String prepareMessage(@NonNull String message, Object... args) {
        String resultString = "Attention!!!  Invalid log format. See exception details above. Message: " +
            message + "; arguments: " + Arrays.toString(args);
        try {
            resultString = String.format(Locale.US, message, args);
        } catch (Throwable e) {
            YLogger.error(TAG, e, resultString);
        }
        return resultString;
    }

    @VisibleForTesting
    @NonNull
    IMessageLogConsumer<String> getSingleLineLogConsumer() {
        return singleLineLogConsumer;
    }

    @VisibleForTesting
    @NonNull
    ILogMessageSplitter getLogMessageSplitter() {
        return logMessageSplitter;
    }
}
