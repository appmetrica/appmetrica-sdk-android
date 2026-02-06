package io.appmetrica.analytics.impl;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable;
import io.appmetrica.analytics.coreapi.internal.system.NetworkType;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.impl.utils.MapWithDefault;
import java.util.Locale;

public final class PhoneUtils {

    // Prevent instantiation
    private PhoneUtils() {}

    @NonNull
    public static String normalizedLocale(@NonNull Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();

        StringBuilder sb = new StringBuilder(language);
        String script = locale.getScript();
        if (!StringUtils.isNullOrEmpty(script)) {
            sb.append('-').append(script);
        }
        if (!StringUtils.isNullOrEmpty(country)) {
            sb.append('_').append(country);
        }
        return sb.toString();
    }

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
                put(ConnectivityManager.TYPE_VPN, NetworkType.VPN);
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

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.M)
    private static boolean isOfflineBeforeQ(@NonNull ConnectivityManager connectivityManager,
                                            @Nullable Network activeNetwork) {
        if (activeNetwork == null) {
            return true;
        }
        final NetworkInfo activeNetworkInfo = connectivityManager.getNetworkInfo(activeNetwork);
        return activeNetworkInfo != null && activeNetworkInfo.isConnected() == false;
    }

    @SuppressWarnings("deprecation")
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
