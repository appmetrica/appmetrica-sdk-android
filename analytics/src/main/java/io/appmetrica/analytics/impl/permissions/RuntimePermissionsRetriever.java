package io.appmetrica.analytics.impl.permissions;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class RuntimePermissionsRetriever implements PermissionRetriever {

    private static final String TAG = "[RuntimePermissionsRetriever]";

    private final Context mContext;
    private final SafePackageManager mSafePackageManager;

    public RuntimePermissionsRetriever(Context context) {
        this(context, new SafePackageManager());
    }

    @VisibleForTesting
    public RuntimePermissionsRetriever(Context context, @NonNull SafePackageManager safePackageManager) {
        mContext = context;
        mSafePackageManager = safePackageManager;
    }

    @NonNull
    public List<PermissionState> getPermissionsState() {
        ArrayList<PermissionState> stateList = new ArrayList<PermissionState>();
        PackageInfo packageInfo = mSafePackageManager.getPackageInfo(
                mContext,
                mContext.getPackageName(),
                PackageManager.GET_PERMISSIONS
        );
        if (packageInfo == null) {
            return stateList;
        }
        final String[] permissions = packageInfo.requestedPermissions;
        int[] permissionFlags = packageInfo.requestedPermissionsFlags;
        if (permissions == null) {
            return stateList;
        }
        for (int i = 0; i < permissions.length; i++) {
            String permissionName = permissions[i];
            if (permissionFlags != null && permissionFlags.length > i &&
                    (permissionFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                YLogger.debug(TAG, "Permission %s is granted", permissionName);
                stateList.add(new PermissionState(permissionName, true));
            } else {
                YLogger.debug(TAG, "Permission %s is not granted", permissionName);
                stateList.add(new PermissionState(permissionName, false));
            }
        }
        return stateList;
    }
}
