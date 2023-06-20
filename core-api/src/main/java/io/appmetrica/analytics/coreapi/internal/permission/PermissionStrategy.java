package io.appmetrica.analytics.coreapi.internal.permission;

import androidx.annotation.NonNull;

public interface PermissionStrategy {

    boolean forbidUsePermission(@NonNull String permission);

}
