package io.appmetrica.analytics.coreapi.internal.permission;

import android.content.Context;
import androidx.annotation.NonNull;

public interface PermissionResolutionStrategy {

    boolean hasNecessaryPermissions(@NonNull Context context);

}
