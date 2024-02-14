package io.appmetrica.analytics.impl.utils;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.internal.AppMetricaService;

public class ManifestUtils {

    private static final SafePackageManager sSafePackageManager = new SafePackageManager();

    private ManifestUtils() {}

    public static final class SwitchOnComponentsRunnable implements Runnable {

        final Context mCtx;

        public SwitchOnComponentsRunnable(final Context ctx) {
            mCtx = ctx;
        }

        @Override
        public void run() {
            enableManifestComponents(mCtx);
        }

    }

    public static void setComponentEnabled(final Context ctx, final ComponentName component) {
        sSafePackageManager.setComponentEnabledSetting(
                ctx, component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        );
    }

    public static void enableManifestComponents(final Context ctx) {
        enableManifestServiceComponent(ctx);
    }

    @SuppressWarnings("WrongConstant") @SuppressLint("InlinedApi")
    public static void enableManifestServiceComponent(final Context ctx) {
        try {
            final PackageInfo packageInfo = sSafePackageManager.getPackageInfo(ctx,
                ctx.getPackageName(), PackageManager.GET_SERVICES | PackageManager.MATCH_DISABLED_COMPONENTS
            );

            if (null != packageInfo.services) {
                for (final ServiceInfo service : packageInfo.services) {
                    if (AppMetricaService.class.getName().equals(service.name) && !service.enabled) {
                        setComponentEnabled(ctx, new ComponentName(ctx, AppMetricaService.class));
                    }
                }
            }
        } catch (Throwable e) {
            /** Do nothing */
        }
    }
}
