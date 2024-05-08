package io.appmetrica.analytics.impl.permissions;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy;
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor;
import io.appmetrica.analytics.logger.internal.YLogger;

public class SimplePermissionExtractor implements PermissionExtractor {

    public static final String TAG = "[SimplePermissionExtractor]";

    @NonNull
    private final PermissionStrategy shouldAskSystemStrategy;

    public SimplePermissionExtractor(@NonNull PermissionStrategy shouldAskSystemStrategy) {
        this.shouldAskSystemStrategy = shouldAskSystemStrategy;
    }

    @Override
    public boolean hasPermission(@NonNull Context context, @NonNull String permission) {
        if (getShouldAskSystemStrategy().forbidUsePermission(permission)) {
            YLogger.debug(TAG, "Should ask system strategy restrict using permission \"%s\"", permission);
            return false;
        }
        return ContextPermissionChecker.hasPermission(context, permission);
    }

    @VisibleForTesting
    @NonNull
    public PermissionStrategy getShouldAskSystemStrategy() {
        return shouldAskSystemStrategy;
    }

}
