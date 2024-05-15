package io.appmetrica.analytics.impl.telephony;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable;
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import io.appmetrica.analytics.coreutils.internal.cache.CachedDataProvider;
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SimInfoExtractor implements TelephonyInfoExtractor<List<SimInfo>> {
    private static final String TAG = "[SimInfoExtractor]";

    private static final long CACHE_EXPIRY_TIME = TimeUnit.SECONDS.toMillis(20);
    @NonNull
    private final Context context;
    @NonNull
    private final PermissionExtractor permissionExtractor;
    @NonNull
    private final CachedDataProvider.CachedData<List<SimInfo>> cachedData =
        new CachedDataProvider.CachedData<>(CACHE_EXPIRY_TIME, CACHE_EXPIRY_TIME, "sim-info");

    SimInfoExtractor(@NonNull Context context) {
        this.context = context;
        this.permissionExtractor = GlobalServiceLocator.getInstance().getGeneralPermissionExtractor();
    }

    @Nullable
    @Override
    public synchronized List<SimInfo> extract() {
        List<SimInfo> simInfos = cachedData.getData();
        if (simInfos == null || simInfos.isEmpty() && cachedData.shouldUpdateData()) {
            simInfos = extractInternal();
            cachedData.setData(simInfos);
        }
        DebugLogger.info(TAG, "Extract simInfo returns %s", simInfos);
        return simInfos;
    }

    private List<SimInfo> extractInternal() {
        List<SimInfo> simInfos = new ArrayList<>();
        if (canCollectSimInfo()) {
            if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.M)) {
                if (permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                    simInfos.addAll(
                        SimInfoExtractorForM.extractSimInfosFromSubscriptionManager(context)
                    );
                } else {
                    DebugLogger.info(TAG, "No READ_PHONE_STATE permission");
                }
                if (simInfos.size() == 0) {
                    simInfos.add(createSimInfo());
                }
            } else {
                simInfos.add(createSimInfo());
            }
        } else {
            DebugLogger.info(TAG, "Sim info collecting forbidden by startup.");
        }
        return simInfos;
    }

    private SimInfo createSimInfo() {
        return new SimInfo(getSimMcc(), getSimMnc(), isNetworkRoaming(), getSimOperatorName());
    }

    @Nullable
    private Integer getSimMcc() {
        return SystemServiceUtils.accessSystemServiceByNameSafely(
            context,
            Context.TELEPHONY_SERVICE,
            "getting SimMcc",
            "TelephonyManager",
            new FunctionWithThrowable<TelephonyManager, Integer>() {
                @Override
                public Integer apply(@NonNull TelephonyManager input) throws Throwable {
                    String simMcc = null;
                    final String simOperator = input.getSimOperator();
                    if (TextUtils.isEmpty(simOperator) == false) {
                        simMcc = simOperator.substring(0, 3);
                    }
                    return TextUtils.isEmpty(simMcc) ? null : Integer.parseInt(simMcc);
                }
            }
        );
    }

    @Nullable
    private Integer getSimMnc() {
        return SystemServiceUtils.accessSystemServiceByNameSafely(
            context,
            Context.TELEPHONY_SERVICE,
            "getting SimMnc",
            "TelephonyManager",
            new FunctionWithThrowable<TelephonyManager, Integer>() {
                @Override
                public Integer apply(@NonNull TelephonyManager input) throws Throwable {
                    String simMnc = null;
                    final String simOperator = input.getSimOperator();
                    if (TextUtils.isEmpty(simOperator) == false) {
                        simMnc = simOperator.substring(3);
                    }
                    return TextUtils.isEmpty(simMnc) ? null : Integer.parseInt(simMnc);
                }
            }
        );
    }

    @Nullable
    private String getSimOperatorName() {
        return SystemServiceUtils.accessSystemServiceByNameSafely(
            context,
            Context.TELEPHONY_SERVICE,
            "getting SimOperatorName",
            "TelephonyManager",
            new FunctionWithThrowable<TelephonyManager, String>() {
                @Override
                public String apply(@NonNull TelephonyManager input) throws Throwable {
                    return input.getSimOperatorName();
                }
            }
        );
    }

    private boolean isNetworkRoaming() {
        return SystemServiceUtils.accessSystemServiceByNameSafelyOrDefault(
            context,
            Context.TELEPHONY_SERVICE,
            "getting NetworkRoaming",
            "TelephonyManager",
            false,
            new FunctionWithThrowable<TelephonyManager, Boolean>() {
                @Override
                public Boolean apply(@NonNull TelephonyManager input) throws Throwable {
                    if (permissionExtractor.hasPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                        return input.isNetworkRoaming();
                    }
                    return null;
                }
            }
        );
    }

    boolean canCollectSimInfo() {
        return GlobalServiceLocator.getInstance().getStartupStateHolder().getStartupState()
            .getCollectingFlags().simInfo;
    }
}
