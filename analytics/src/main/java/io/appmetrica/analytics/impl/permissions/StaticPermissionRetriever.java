package io.appmetrica.analytics.impl.permissions;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.ArrayList;
import java.util.List;

public class StaticPermissionRetriever implements PermissionRetriever {

    private static final String TAG = "[StaticPermissionRetriever]";

    @NonNull private final Context mContext;
    @NonNull private final String mPackageName;
    @NonNull private final SafePackageManager mSafePackageManager;

    public StaticPermissionRetriever(@NonNull final Context context) {
        this(context, context.getPackageName(), new SafePackageManager());
    }

    public StaticPermissionRetriever(@NonNull final Context context,
                                     @NonNull final String packageName,
                                     @NonNull SafePackageManager safePackageManager) {
        mContext = context;
        mPackageName = packageName;
        mSafePackageManager = safePackageManager;
    }

    @NonNull
    public List<PermissionState> getPermissionsState() {
        ArrayList<PermissionState> stateList = new ArrayList<PermissionState>();
        PackageInfo packageInfo =
                mSafePackageManager.getPackageInfo(mContext, mPackageName, PackageManager.GET_PERMISSIONS);
        if (packageInfo != null) {
            for (String permissionName : packageInfo.requestedPermissions) {
                YLogger.debug(TAG, "has Permission %s", permissionName);
                stateList.add(new PermissionState(permissionName, true));
            }
        }
        return stateList;
    }
}
