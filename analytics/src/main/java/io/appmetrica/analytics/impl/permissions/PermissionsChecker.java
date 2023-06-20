package io.appmetrica.analytics.impl.permissions;

import android.content.Context;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import java.util.List;

public class PermissionsChecker {

    public @Nullable List<PermissionState> check(Context context, @Nullable List<PermissionState> fromDb) {
        List<PermissionState> fromSystem = createPermissionsRetriever(context).getPermissionsState();
        if (CollectionUtils.areCollectionsEqual(fromSystem, fromDb)) {
            return null;
        } else {
            return fromSystem;
        }
    }

    @VisibleForTesting
    PermissionRetriever createPermissionsRetriever(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
            return new RuntimePermissionsRetriever(context);
        } else {
            return new StaticPermissionRetriever(context);
        }
    }

}
