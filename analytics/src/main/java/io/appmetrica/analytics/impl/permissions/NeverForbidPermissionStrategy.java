package io.appmetrica.analytics.impl.permissions;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy;

public class NeverForbidPermissionStrategy implements PermissionStrategy {

    @Override
    public boolean forbidUsePermission(@NonNull String permission) {
        return false;
    }

    @Override
    public String toString() {
        return "AlwaysAskForPermissionStrategy{always allow permission}";
    }
}
