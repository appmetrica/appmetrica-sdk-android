package io.appmetrica.analytics.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BooleanResourceRetriever extends ResourceRetriever<Boolean> {

    public BooleanResourceRetriever(@NonNull Context context, @NonNull String resourceName) {
        super(context, resourceName, "bool");
    }

    @Nullable
    @Override
    protected Boolean callAppropriateMethod(int resourceId) {
        return mContext.getResources().getBoolean(resourceId);
    }
}
