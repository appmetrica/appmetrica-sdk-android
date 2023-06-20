package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;

public class BackgroundRestrictionsStateProvider {

    @NonNull
    private final Context mContext;
    @NonNull
    private final AppStandbyBucketConverter mConverter;

    public BackgroundRestrictionsStateProvider(@NonNull Context context) {
        this(context, new AppStandbyBucketConverter());
    }

    @VisibleForTesting
    BackgroundRestrictionsStateProvider(@NonNull Context context,
                                        @NonNull AppStandbyBucketConverter converter) {
        mContext = context;
        mConverter = converter;
    }

    @Nullable
    public BackgroundRestrictionsState getBackgroundRestrictionsState() {
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.P)) {
            return BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(mContext, mConverter);
        } else {
            return null;
        }
    }
}
