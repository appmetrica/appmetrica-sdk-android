package io.appmetrica.analytics.coreutils.internal.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import io.appmetrica.analytics.logger.internal.YLogger;

public class SafePackageManager {
    private static final String TAG = "[SafePackageManager]";

    @Nullable
    public PackageInfo getPackageInfo(Context context, String packageName) {
        return getPackageInfo(context, packageName, 0);
    }

    @Nullable
    public PackageInfo getPackageInfo(Context context, String packageName, int flags) {
        PackageInfo packageInfo = null;
        try {
            PackageManager pm = context.getPackageManager();
            packageInfo = pm.getPackageInfo(packageName, flags);
        } catch (Throwable e) {
            YLogger.e(e, e.getMessage());
        }
        return packageInfo;
    }

    @Nullable
    public ServiceInfo getServiceInfo(@NonNull Context context, ComponentName component, int flags) {
        ServiceInfo result = null;
        try {
            PackageManager pm = context.getPackageManager();
            result = pm.getServiceInfo(component, flags);
        } catch (Throwable e) {
            YLogger.e(e, e.getMessage());
        }
        return result;
    }

    @Nullable
    public ResolveInfo resolveService(@NonNull Context context, Intent intent, int flags) {
        ResolveInfo result = null;
        try {
            PackageManager pm = context.getPackageManager();
            result = pm.resolveService(intent, flags);
        } catch (Throwable e) {
            YLogger.e(e, e.getMessage());
        }
        return result;
    }

    @Nullable
    public ResolveInfo resolveActivity(@NonNull Context context, @NonNull Intent intent, int flags) {
        ResolveInfo result = null;
        try {
            PackageManager pm = context.getPackageManager();
            result = pm.resolveActivity(intent, flags);
        } catch (Throwable e) {
            YLogger.e(e, e.getMessage());
        }
        return result;
    }

    @Nullable
    public ApplicationInfo getApplicationInfo(@NonNull Context context, String packageName, int flags) {
        ApplicationInfo result = null;
        try {
            PackageManager pm = context.getPackageManager();
            result = pm.getApplicationInfo(packageName, flags);
        } catch (Throwable e) {
            YLogger.e(e, e.getMessage());
        }
        return result;
    }

    @Nullable
    public ActivityInfo getActivityInfo(@NonNull Context context,
                                        @NonNull ComponentName componentName,
                                        final int flags) {
        ActivityInfo result = null;
        try {
            PackageManager pm = context.getPackageManager();
            result = pm.getActivityInfo(componentName, flags);
        } catch (Throwable e) {
            YLogger.e(e, e.getMessage());
        }
        return result;
    }

    public void setComponentEnabledSetting(@NonNull Context context,
                                           ComponentName componentName,
                                           int newState,
                                           int flags) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.setComponentEnabledSetting(componentName, newState, flags);
        } catch (Throwable e) {
            YLogger.e(e, e.getMessage());
        }
    }

    public boolean hasSystemFeature(@NonNull Context context, @NonNull String name) {
        boolean result = false;
        try {
            PackageManager pm = context.getPackageManager();
            result = pm.hasSystemFeature(name);
        } catch (Throwable e) {
            YLogger.e(e, e.getMessage());
        }
        return result;
    }

    @Nullable
    public String getInstallerPackageName(@NonNull Context context, @NonNull String packageName) {
        String result = null;
        try {
            PackageManager pm = context.getPackageManager();
            if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.R)) {
                result = SafePackageManagerHelperForR.extractPackageInstaller(pm, packageName);
            } else {
                result = pm.getInstallerPackageName(packageName);
            }

        } catch (Throwable e) {
            YLogger.e(e, e.getMessage());
        }
        YLogger.info(TAG, "AppInstaller = %s", result);
        return result;
    }
}
