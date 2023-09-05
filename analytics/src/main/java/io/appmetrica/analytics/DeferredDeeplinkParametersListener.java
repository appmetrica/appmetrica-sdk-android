package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import java.util.Map;

/**
 * Callback for {@link AppMetrica#requestDeferredDeeplinkParameters(DeferredDeeplinkParametersListener)}.
 */
public interface DeferredDeeplinkParametersListener {

    /**
     * Possible values for {@link DeferredDeeplinkParametersListener#onError(Error, String)}
     */
    enum Error {
        /**
         * Tells that Google Play referrer wasn't obtained because it can be requested during first launch only
         */
        NOT_A_FIRST_LAUNCH("Deferred deeplink parameters can be requested during first launch only."),
        /**
         * Tells that Google Play referrer was obtained but it did not contain valid deferred deeplink parameters
         */
        PARSE_ERROR("Google Play referrer did not contain valid deferred deeplink parameters."),
        /**
         * Means that referrer was not obtained (because there was no provider (Google Play Services, Huawei Media Services)
         * on device or because the provider returned null)
         */
        NO_REFERRER("No referrer was found"),
        /**
         * Could not obtain deferred deeplink parameters due to unknown error
         */
        UNKNOWN("Unknown error");

        private final String mDescription;

        Error(String description) {
            mDescription = description;
        }

        /**
         * @return String value for {@link Error}
         */
        public String getDescription() {
            return mDescription;
        }
    }

    /**
     * Called when deferred deeplink parameters requested in
     * {@link AppMetrica#requestDeferredDeeplinkParameters(DeferredDeeplinkParametersListener)} are obtained
     *
     * @param parameters {@link Map} with obtained deferred deeplink parameters
     */
    void onParametersLoaded(@NonNull Map<String, String> parameters);

    /**
     * Called when error occurs during deferred deeplink parameters obtaining by
     * {@link AppMetrica#requestDeferredDeeplinkParameters(DeferredDeeplinkParametersListener)}
     *
     * @param error error which tells why deferred deeplink parameters were not obtained
     * @param referrer Google Play referrer in case of {@link Error#PARSE_ERROR}
     */
    void onError(@NonNull Error error, @NonNull String referrer);
}
