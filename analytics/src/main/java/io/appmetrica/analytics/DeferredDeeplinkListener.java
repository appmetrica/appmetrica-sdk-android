package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface DeferredDeeplinkListener {

    /**
     * Possible values for {@link DeferredDeeplinkListener#onError(Error, String)}
     */
    enum Error {
        /**
         * Tells that Google Play referrer wasn't obtained because it can be requested during first launch only
         */
        NOT_A_FIRST_LAUNCH("Deferred deeplink can be requested during first launch only."),
        /**
         * Tells that Google Play referrer was obtained but it did not contain deferred deeplink
         */
        PARSE_ERROR("Google Play referrer did not contain deferred deeplink."),
        /**
         * Means that referrer was not obtained (because there was no provider (Google Play Services, Huawei Media Services)
         * on device or because the provider returned null)
         */
        NO_REFERRER("No referrer was found"),
        /**
         * Could not obtain deferred deeplink due to unknown error
         */
        UNKNOWN("Unknown error");

        private final String mDescription;

        Error(String description) {
            mDescription = description;
        }

        public String getDescription() {
            return mDescription;
        }
    }

    /**
     * Called when deferred deeplink requested in
     * {@link AppMetrica#requestDeferredDeeplink(DeferredDeeplinkListener)} is obtained
     *
     * @param deeplink {@link String} obtained deferred deeplink
     */
    void onDeeplinkLoaded(@NonNull String deeplink);

    /**
     * Called when error occurs during deferred deeplink obtaining by
     * {@link AppMetrica#requestDeferredDeeplink(DeferredDeeplinkListener)}
     *
     * @param error error which tells why deferred deeplink parameters were not obtained
     * @param referrer Google Play referrer in case of {@link Error#PARSE_ERROR}
     */
    void onError(@NonNull Error error, @Nullable String referrer);
}
