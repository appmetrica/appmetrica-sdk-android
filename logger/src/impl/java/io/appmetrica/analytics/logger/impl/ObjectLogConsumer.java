package io.appmetrica.analytics.logger.impl;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

class ObjectLogConsumer<T> implements IMessageLogConsumer<T> {

    @NonNull
    private final MultilineMessageLogConsumer messageLogConsumer;
    @NonNull
    private final IObjectLogDumper<T> objectLogDumper;

    public ObjectLogConsumer(@NonNull MultilineMessageLogConsumer messageLogConsumer,
                             @NonNull IObjectLogDumper<T> objectLogDumper) {
        this.messageLogConsumer = messageLogConsumer;
        this.objectLogDumper = objectLogDumper;
    }

    @Override
    public void consumeWithTag(@NonNull String tag, @NonNull T message, Object... args) {
        messageLogConsumer.consumeWithTag(tag, objectLogDumper.dumpObject(message), args);
    }

    @Override
    public void consume(@NonNull T message, Object... args) {
        messageLogConsumer.consume(objectLogDumper.dumpObject(message), args);
    }

    @VisibleForTesting
    @NonNull
    MultilineMessageLogConsumer getMessageLogConsumer() {
        return messageLogConsumer;
    }

    @VisibleForTesting
    @NonNull
    IObjectLogDumper<T> getObjectLogDumper() {
        return objectLogDumper;
    }
}
