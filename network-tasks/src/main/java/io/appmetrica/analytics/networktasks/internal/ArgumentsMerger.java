package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.NonNull;

public interface ArgumentsMerger<I, O> {

    @NonNull
    O mergeFrom(@NonNull I other);

    boolean compareWithOtherArguments(@NonNull I other);

}
