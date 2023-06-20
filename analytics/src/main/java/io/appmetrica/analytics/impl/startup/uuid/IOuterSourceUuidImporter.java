package io.appmetrica.analytics.impl.startup.uuid;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface IOuterSourceUuidImporter {

    @Nullable
    String get(@NonNull Context context);

}
