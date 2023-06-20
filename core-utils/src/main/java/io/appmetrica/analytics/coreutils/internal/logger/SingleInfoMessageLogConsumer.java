package io.appmetrica.analytics.coreutils.internal.logger;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

class SingleInfoMessageLogConsumer implements IMessageLogConsumer<String> {

    @NonNull
    private final BaseLogger logger;

    public SingleInfoMessageLogConsumer(@NonNull BaseLogger logger) {
        this.logger = logger;
    }

    @Override
    public void consume(@NonNull String message, Object... args) {
        logger.fi(message, args);
    }

    @Override
    public void consumeWithTag(@NonNull String tag, @NonNull String message, Object... args) {
        logger.fi(tag + message, args);
    }

    @VisibleForTesting
    @NonNull
    public BaseLogger getLogger() {
        return logger;
    }
}
