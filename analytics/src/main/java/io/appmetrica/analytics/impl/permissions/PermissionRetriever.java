package io.appmetrica.analytics.impl.permissions;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState;
import java.util.List;

public interface PermissionRetriever {

    @NonNull
    List<PermissionState> getPermissionsState();

}
