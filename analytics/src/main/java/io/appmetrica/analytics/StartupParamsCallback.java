package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.startup.Constants;
import java.util.Map;

/**
 * Callback to notify about params' changes.
 */
public interface StartupParamsCallback {

    /**
     * The key for uuid at [List] in [AppMetrica.requestStartupParams]
     */
    String YANDEX_MOBILE_METRICA_UUID =
            Constants.StartupParamsCallbackKeys.UUID;

    /**
     * The key for device id at [List] in [AppMetrica.requestStartupParams]
     */
    String YANDEX_MOBILE_METRICA_DEVICE_ID =
            Constants.StartupParamsCallbackKeys.DEVICE_ID;

    /**
     * The key for device id hash at [List] in [AppMetrica.requestStartupParams]
     */
     String APP_METRICA_DEVICE_ID_HASH =
            Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH;

    /**
     * Objects of this class contain information about retrieved startup parameters.
     */
    final class Result {

        @NonNull
        public final Map<String, IdentifiersResult> parameters;
        @Nullable
        public final String uuid;
        @Nullable
        public final String deviceId;
        @Nullable
        public final String deviceIdHash;

        public Result(@NonNull final Map<String, IdentifiersResult> parameters) {
            this.parameters = parameters;
            this.uuid = parameterForKey(YANDEX_MOBILE_METRICA_UUID);
            this.deviceId = parameterForKey(YANDEX_MOBILE_METRICA_DEVICE_ID);
            this.deviceIdHash = parameterForKey(APP_METRICA_DEVICE_ID_HASH);

        }

        /**
         * Get parameter as [String] by [key]
         *
         * @param key the reason explaining why startup params could not be obtained.
         * @return [String] or null if no such key in params
         */
        @Nullable
        public String parameterForKey(@NonNull final String key) {
            final IdentifiersResult result = parameters.get(key);
            if (result != null) {
                return result.id;
            } else {
                return null;
            }
        }
    }

    final class Reason {

        /**
         * Could not obtain identifiers due to unknown error.
         */
        public static Reason UNKNOWN = new Reason("UNKNOWN");

        /**
         * Could not obtain identifiers due to network error.
         */
        public static Reason NETWORK = new Reason("NETWORK");

        /**
         * Could not obtain identifier due to error parsing server response.
         */
        public static Reason INVALID_RESPONSE = new Reason("INVALID_RESPONSE");

        @NonNull
        public final String value;

        public Reason(@NonNull final String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Reason reason = (Reason) o;
            return value.equals(reason.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public String toString() {
            return "Reason{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }

    /**
     * Called when the startup params (Uuid, DeviceId, Urls, Clids) are obtained.
     * @param result the {@link Result} containing startup params.
     */
    void onReceive(@Nullable final Result result);

    /**
     * Called when the startup params couldn't be obtained for some reason.
     * @param reason the reason explaining why startup params could not be obtained.
     * @param partialResult the {@link Result} containing startup params that could be obtained.
     * For example,
     */
    void onRequestError(@NonNull final Reason reason, @Nullable final Result partialResult);
}
