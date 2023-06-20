package io.appmetrica.analytics.coreutils.internal.logger;

import androidx.annotation.NonNull;

interface IObjectLogDumper<T> {

    String dumpObject(@NonNull T input);

}
