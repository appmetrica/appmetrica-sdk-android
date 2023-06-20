package io.appmetrica.analytics.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StringResourceRetriever extends ResourceRetriever<String> {

    public StringResourceRetriever(@NonNull Context context, @NonNull String resourceName) {
        super(context, resourceName, "string");
    }

    @Nullable
    @Override
    protected String callAppropriateMethod(int resourceId) {
        return mContext.getString(resourceId);
    }
}
