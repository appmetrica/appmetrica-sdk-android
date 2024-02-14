package io.appmetrica.analytics.logger.impl;

import androidx.annotation.NonNull;

interface IObjectLogDumper<T> {

    String dumpObject(@NonNull T input);

}
