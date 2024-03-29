package io.appmetrica.analytics.logger.impl;

import androidx.annotation.NonNull;

interface IMessageLogConsumer<T> {

    void consumeWithTag(@NonNull final String tag, @NonNull final T message, Object... args);

    void consume(@NonNull final T message, Object... args);

}
