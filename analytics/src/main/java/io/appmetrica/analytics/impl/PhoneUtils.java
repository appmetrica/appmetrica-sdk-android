package io.appmetrica.analytics.impl;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable;
import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.impl.utils.MapWithDefault;
import java.util.Locale;

public final class PhoneUtils {

    public enum NetworkType {
        WIFI,
        CELL,
        ETHERNET,
        BLUETOOTH,
        VPN,
        LOWPAN,
        WIFI_AWARE,
        MOBILE_DUN,
        MOBILE_HIPRI,
        MOBILE_MMS,
        MOBILE_SUPL,
        WIMAX,
        OFFLINE,
        UNDEFINED
    }

    private static final String TAG = "[PhoneUtils]";
    private static final SafePackageManager sSafePackageManager = new SafePackageManager();

    // Prevent instantiation
    private PhoneUtils() {}

    private static final int MIN_SW_DP_FOR_TABLET = 600;
    private static final int DIAGONAL_INCHES_TV = 15;
    private static final int DIAGONAL_INCHES_TABLET = 7;

    /**
     * Determines if the device is a tablet (tablet, phone?).
     *
     * @param ctx {@link Context} object. The calling context.
     */
    @NonNull
    public static String getDeviceType(@NonNull Context ctx, @NonNull Point realDeviceScreenSize) {
        float density = 0;
        try {
            density = ctx.getResources().getDisplayMetrics().density;
        } catch (Throwable ex) {
            YLogger.error(TAG, ex);
        }
        if (density == 0) {
            return DeviceTypeValues.PHONE;
        }

        final int realDeviceWidth = realDeviceScreenSize.x;
        final int realDeviceHeight = realDeviceScreenSize.y;

        final float widthDp = realDeviceWidth / density;
        final float heightDp = realDeviceHeight / density;
        final float smallestWidthDp = Math.min(widthDp, heightDp);

        float densityDpi = density * 160;
        final float widthInchesApprox = realDeviceWidth / densityDpi;
        final float heightInchesApprox = realDeviceHeight / densityDpi;
        final double diagonalInchesApprox =
            Math.sqrt((widthInchesApprox * widthInchesApprox) + (heightInchesApprox * heightInchesApprox));

        if (isAndroidTV(ctx, diagonalInchesApprox)) {
            return DeviceTypeValues.TV;
        }

        if (diagonalInchesApprox >= DIAGONAL_INCHES_TABLET || smallestWidthDp >= MIN_SW_DP_FOR_TABLET) {
            return DeviceTypeValues.TABLET;
        }

        return DeviceTypeValues.PHONE;
    }

    // Based on information from Android Developers:
    // http://developer.android.com/training/tv/unsupported-features-tv.html
    private static boolean isAndroidTV(Context context, double diagonalInches) {
        return diagonalInches >= DIAGONAL_INCHES_TV
            && !sSafePackageManager.hasSystemFeature(context, "android.hardware.touchscreen");
    }

    @SuppressLint("NewApi")
    @NonNull
    public static String normalizedLocale(@NonNull Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();

        StringBuilder sb = new StringBuilder(language);
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.LOLLIPOP)) {
            String script = locale.getScript();
            if (TextUtils.isEmpty(script) == false) {
                sb.append('-').append(script);
            }
        }
        if (TextUtils.isEmpty(country) == false) {
            sb.append('_').append(country);
        }
        return sb.toString();
    }

    @SuppressLint("InlinedApi")
    private static final MapWithDefault<Integer, NetworkType> CONNECTIVITY_MANAGER_NETWORK_TYPE_MAPPING =
        new MapWithDefault<Integer, NetworkType>(NetworkType.UNDEFINED) {
            {
                put(ConnectivityManager.TYPE_WIFI, NetworkType.WIFI);
                put(ConnectivityManager.TYPE_MOBILE, NetworkType.CELL);
                put(ConnectivityManager.TYPE_BLUETOOTH, NetworkType.BLUETOOTH);
                put(ConnectivityManager.TYPE_ETHERNET, NetworkType.ETHERNET);
                put(ConnectivityManager.TYPE_MOBILE_DUN, NetworkType.MOBILE_DUN);
                put(ConnectivityManager.TYPE_MOBILE_HIPRI, NetworkType.MOBILE_HIPRI);
                put(ConnectivityManager.TYPE_MOBILE_MMS, NetworkType.MOBILE_MMS);
                put(ConnectivityManager.TYPE_MOBILE_SUPL, NetworkType.MOBILE_SUPL);
                put(ConnectivityManager.TYPE_WIMAX, NetworkType.WIMAX);
                if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.LOLLIPOP)) {
                    put(ConnectivityManager.TYPE_VPN, NetworkType.VPN);
                }
            }
        };

    @TargetApi(Build.VERSION_CODES.M)
    @SuppressLint("InlinedApi")
    private static final MapWithDefault<Integer, NetworkType> NETWORK_CAPABILITIES_TRANSPORT_TYPE_MAPPING =
        new MapWithDefault<Integer, NetworkType>(NetworkType.UNDEFINED) {
            {
                put(NetworkCapabilities.TRANSPORT_WIFI, NetworkType.WIFI);
                put(NetworkCapabilities.TRANSPORT_CELLULAR, NetworkType.CELL);
                put(NetworkCapabilities.TRANSPORT_ETHERNET, NetworkType.ETHERNET);
                put(NetworkCapabilities.TRANSPORT_BLUETOOTH, NetworkType.BLUETOOTH);
                put(NetworkCapabilities.TRANSPORT_VPN, NetworkType.VPN);
                if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.O_MR1)) {
                    put(NetworkCapabilities.TRANSPORT_LOWPAN, NetworkType.LOWPAN);
                }
                if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.O)) {
                    put(NetworkCapabilities.TRANSPORT_WIFI_AWARE, NetworkType.WIFI_AWARE);
                }
            }
        };

    private static final MapWithDefault<NetworkType, Integer> NETWORK_TYPE_TO_SERVER_FORMAT_MAPPING =
        new MapWithDefault<NetworkType, Integer>(EventProto.ReportMessage.Session.CONNECTION_UNDEFINED) {
            {
                put(NetworkType.CELL, EventProto.ReportMessage.Session.CONNECTION_CELL);
                put(NetworkType.WIFI, EventProto.ReportMessage.Session.CONNECTION_WIFI);
                put(NetworkType.BLUETOOTH, EventProto.ReportMessage.Session.CONNECTION_BLUETOOTH);
                put(NetworkType.ETHERNET, EventProto.ReportMessage.Session.CONNECTION_ETHERNET);
                put(NetworkType.MOBILE_DUN, EventProto.ReportMessage.Session.CONNECTION_MOBILE_DUN);
                put(NetworkType.MOBILE_HIPRI, EventProto.ReportMessage.Session.CONNECTION_MOBILE_HIPRI);
                put(NetworkType.MOBILE_MMS, EventProto.ReportMessage.Session.CONNECTION_MOBILE_MMS);
                put(NetworkType.MOBILE_SUPL, EventProto.ReportMessage.Session.CONNECTION_MOBILE_SUPL);
                put(NetworkType.VPN, EventProto.ReportMessage.Session.CONNECTION_VPN);
                put(NetworkType.WIMAX, EventProto.ReportMessage.Session.CONNECTION_WIMAX);
                put(NetworkType.LOWPAN, EventProto.ReportMessage.Session.CONNECTION_LOWPAN);
                put(NetworkType.WIFI_AWARE, EventProto.ReportMessage.Session.CONNECTION_WIFI_AWARE);
            }
        };

    @NonNull
    public static NetworkType getConnectionType(@NonNull final Context context) {
        final ConnectivityManager connManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return SystemServiceUtils.accessSystemServiceSafelyOrDefault(
            connManager,
            "getting connection type",
            "ConnectivityManager",
            NetworkType.UNDEFINED,
            new FunctionWithThrowable<ConnectivityManager, NetworkType>() {
                @Override
                public NetworkType apply(@NonNull ConnectivityManager input) throws Throwable {
                    if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.M)) {
                        return getNetworkTypeForM(input);
                    } else {
                        return getNetworkTypeBeforeM(input);
                    }
                }
            }
        );
    }

    @TargetApi(Build.VERSION_CODES.M)
    @NonNull
    private static NetworkType getNetworkTypeForM(@NonNull ConnectivityManager connectivityManager) {
        NetworkType result = NetworkType.UNDEFINED;
        final Network activeNetwork = connectivityManager.getActiveNetwork();
        if (isOffline(connectivityManager, activeNetwork)) {
            result = NetworkType.OFFLINE;
        } else {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            if (networkCapabilities != null) {
                for (Integer type : NETWORK_CAPABILITIES_TRANSPORT_TYPE_MAPPING.keySet()) {
                    if (networkCapabilities.hasTransport(type)) {
                        result = NETWORK_CAPABILITIES_TRANSPORT_TYPE_MAPPING.get(type);
                        break;
                    }
                }
            }
        }
        return result;
    }

    private static boolean isOffline(@NonNull ConnectivityManager connectivityManager,
                                     @Nullable Network activeNetwork) {
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)) {
            return activeNetwork == null;
        } else {
            return isOfflineBeforeQ(connectivityManager, activeNetwork);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static boolean isOfflineBeforeQ(@NonNull ConnectivityManager connectivityManager,
                                            @Nullable Network activeNetwork) {
        if (activeNetwork == null) {
            return true;
        }
        final NetworkInfo activeNetworkInfo = connectivityManager.getNetworkInfo(activeNetwork);
        return activeNetworkInfo != null && activeNetworkInfo.isConnected() == false;
    }

    @NonNull
    private static NetworkType getNetworkTypeBeforeM(@NonNull ConnectivityManager connManager) {
        NetworkType result;
        final NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            result = CONNECTIVITY_MANAGER_NETWORK_TYPE_MAPPING.get(activeNetworkInfo.getType());
        } else {
            result = NetworkType.OFFLINE;
        }
        return result;
    }

    /**
     * @return network connection type in the server format
     * (Connection type: {@code CELL}, {@code WIFI}, {@code BLUETOOTH}, {@code ETHERNET}, {@code MOBILE_DUN},
     * {@code MOBILE_HIPRI}, {@code MOBILE_MMS}, {@code MOBILE_SUPL}, {@code VPN}, {@code WIMAX}, {@code LOWPAN}, {@code WIFI_AWARE};
     */
    public static int getConnectionTypeInServerFormat(@NonNull final Context ctx) {
        return getConnectionTypeInServerFormat(getConnectionType(ctx));
    }

    @VisibleForTesting
    static int getConnectionTypeInServerFormat(@Nullable NetworkType networkType) {
        return NETWORK_TYPE_TO_SERVER_FORMAT_MAPPING.get(networkType);
    }
}
