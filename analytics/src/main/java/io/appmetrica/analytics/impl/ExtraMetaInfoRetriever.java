package io.appmetrica.analytics.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.logger.internal.DebugLogger;

public class ExtraMetaInfoRetriever {

    private static final String TAG = "[ExtraMetaInfoRetriever]";

    private static final String BUILD_ID_RESOURCE_NAME = "io.appmetrica.analytics.build_id";
    private static final String IS_OFFLINE_RESOURCE_NAME = "io.appmetrica.analytics.is_offline";

    @NonNull
    private final StringResourceRetriever mBuildIdRetriever;
    @NonNull
    private final BooleanResourceRetriever mIsOfflineRetriever;

    public ExtraMetaInfoRetriever(@NonNull Context context) {
        this(
                new StringResourceRetriever(context, BUILD_ID_RESOURCE_NAME),
                new BooleanResourceRetriever(context, IS_OFFLINE_RESOURCE_NAME)
        );
    }

    @VisibleForTesting
    ExtraMetaInfoRetriever(@NonNull StringResourceRetriever buildIdRetriever,
                           @NonNull BooleanResourceRetriever isOfflineRetriever) {
        mBuildIdRetriever = buildIdRetriever;
        mIsOfflineRetriever = isOfflineRetriever;
    }

    @Nullable
    public String getBuildId() {
        String buildId =  mBuildIdRetriever.getResource();
        DebugLogger.info(TAG, "Retrieved build_id: %s", buildId);
        return buildId;
    }

    @Nullable
    public Boolean isOffline() {
        return mIsOfflineRetriever.getResource();
    }
}
