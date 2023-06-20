package io.appmetrica.analytics.coreutils.internal.logger;

import androidx.annotation.NonNull;

interface IMessageLogConsumer<T> {

    void consumeWithTag(@NonNull final String tag, @NonNull final T message, Object... args);

    void consume(@NonNull final T message, Object... args);

}
