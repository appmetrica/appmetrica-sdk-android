package io.appmetrica.analytics.impl.permissions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState;
import io.appmetrica.analytics.impl.BackgroundRestrictionsState;
import java.util.List;

public class AppPermissionsState {

    @NonNull
    public final List<PermissionState> mPermissionStateList;
    @Nullable
    public final BackgroundRestrictionsState mBackgroundRestrictionsState;
    @NonNull
    public final List<String> mAvailableProviders;

    public AppPermissionsState(@NonNull List<PermissionState> permissionStateList,
                               @Nullable BackgroundRestrictionsState backgroundRestrictionsState,
                               @NonNull List<String> availableProviders) {
        mPermissionStateList = permissionStateList;
        mBackgroundRestrictionsState = backgroundRestrictionsState;
        mAvailableProviders = availableProviders;
    }

    @Override
    public String toString() {
        return "AppPermissionsState{" +
            "mPermissionStateList=" + mPermissionStateList +
            ", mBackgroundRestrictionsState=" + mBackgroundRestrictionsState +
            ", mAvailableProviders=" + mAvailableProviders +
            '}';
    }
}
