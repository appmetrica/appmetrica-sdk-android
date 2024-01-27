package io.appmetrica.analytics.impl;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.model.ScreenInfo;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.service.AppMetricaServiceAction;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.internal.AppMetricaService;

public final class ServiceUtils {

    public static final String PATH_CLIENT = "client";
    public static final String PARAMETER_PID = "pid";
    public static final String PARAMETER_PSID = "psid";
    public static final String EXTRA_SCREEN_SIZE = "screen_size";
    public static final String KEY_VALUE = "value";

    private static final String APPMETRICA_SERVICE_SCHEME = "appmetrica";

    private static final SafePackageManager sSafePackageManager = new SafePackageManager();

    // Prevent instantiation
    private ServiceUtils() {}

    private static Uri createAppMetricaUri(Context context) {
        return new Uri.Builder().scheme(APPMETRICA_SERVICE_SCHEME)
                .authority(context.getPackageName())
                .build();
    }

    public static Intent getBaseIntentToConnect(final Context context) {
        return new Intent(context, AppMetricaService.class)
                .setAction(AppMetricaServiceAction.ACTION_CLIENT_CONNECTION)
                .setData(createAppMetricaUri(context))
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
    }

    public static Intent getOwnMetricaServiceIntent(Context context) {
        final Intent serviceIntent = getBaseIntentToConnect(context).putExtras(retrieveMetaData(context));
        serviceIntent.setData(serviceIntent.getData().buildUpon()
                .path(PATH_CLIENT)
                .appendQueryParameter(PARAMETER_PID, String.valueOf(Process.myPid()))
                .appendQueryParameter(PARAMETER_PSID, ProcessConfiguration.PROCESS_SESSION_ID).build());
        serviceIntent.putExtra(EXTRA_SCREEN_SIZE, getScreenSize(context));
        return serviceIntent.setPackage(context.getApplicationContext().getPackageName());
    }

    private static Bundle retrieveMetaData(final Context context) {
        final String packageName = context.getPackageName();

        try {
            final Bundle metaData = sSafePackageManager
                    .getApplicationInfo(context, packageName, PackageManager.GET_META_DATA).metaData;
            return null == metaData ? new Bundle() : metaData;
        } catch (Throwable e) {
            // Do nothing
        }

        return new Bundle();
    }

    @Nullable
    private static String getScreenSize(@NonNull Context context) {
        ScreenInfo screenInfo = ScreenInfoRetriever.getInstance(context).retrieveScreenInfo();
        return  screenInfo == null ? null : JsonHelper.screenInfoToJsonString(screenInfo);
    }

}
