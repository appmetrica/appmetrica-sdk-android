package io.appmetrica.analytics.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.logger.internal.DebugLogger;

public abstract class ResourceRetriever<T> {

    private static final String TAG = "[ResourceRetriever]";

    @NonNull
    protected final Context mContext;
    @NonNull
    private final String mResourceName;
    @NonNull
    private final String mResourceType;

    public ResourceRetriever(@NonNull Context context,
                             @NonNull String resourceName,
                             @NonNull String resourceType) {
        mContext = context;
        mResourceName = resourceName;
        mResourceType = resourceType;
    }

    @Nullable
    public T getResource() {
        @SuppressLint("DiscouragedApi")
        final int resourceId = mContext.getResources()
                .getIdentifier(mResourceName, mResourceType, mContext.getPackageName());
        if (resourceId != 0) {
            try {
                return callAppropriateMethod(resourceId);
            } catch (Throwable ex) {
                DebugLogger.error(TAG, ex, "Error while parsing " + mResourceName);
            }
        } else {
            DebugLogger.info(TAG, "no " + mResourceName + " resource found.");
        }
        return null;
    }

    @Nullable
    protected abstract T callAppropriateMethod(final int resourceId);
}
