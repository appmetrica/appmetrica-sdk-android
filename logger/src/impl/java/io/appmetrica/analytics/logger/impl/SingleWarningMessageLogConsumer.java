package io.appmetrica.analytics.logger.impl;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

class SingleWarningMessageLogConsumer implements IMessageLogConsumer<String> {

    @NonNull
    private final BaseLogger logger;

    public SingleWarningMessageLogConsumer(@NonNull BaseLogger logger) {
        this.logger = logger;
    }

    @Override
    public void consume(@NonNull String message, Object... args) {
        logger.fw(message, args);
    }

    @Override
    public void consumeWithTag(@NonNull String tag, @NonNull String message, Object... args) {
        logger.fw(tag + message, args);
    }

    @VisibleForTesting
    @NonNull
    BaseLogger getLogger() {
        return logger;
    }
}
