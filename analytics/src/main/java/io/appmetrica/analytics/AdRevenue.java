package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.impl.Utils;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;

/**
 * The class to store Ad Revenue data.
 *
 * <p>The Ad Revenue object should be passed to the AppMetrica by using the
 * {@link AppMetrica#reportAdRevenue(AdRevenue)}
 * or {@link IReporter#reportAdRevenue(AdRevenue)}method.</p>
 */
public class AdRevenue {

    /**
     * Amount of money received via ad revenue.
     * It cannot be negative.
     */
    @NonNull
    public final BigDecimal adRevenue;
    /**
     * Currency in which money from `adRevenue` is represented.
     */
    @NonNull
    public final Currency currency;
    /**
     * Ad type. See possible values in {@link AdType}.
     */
    @Nullable
    public final AdType adType;
    /**
     * Ad network.
     * Maximum length is 100 symbols. If the value exceeds this limit it will be truncated by AppMetrica.
     */
    @Nullable
    public final String adNetwork;
    /**
     * Id of ad unit.
     * Maximum length is 100 symbols. If the value exceeds this limit it will be truncated by AppMetrica.
     */
    @Nullable
    public final String adUnitId;
    /**
     * Name of ad unit.
     * Maximum length is 100 symbols. If the value exceeds this limit it will be truncated by AppMetrica.
     */
    @Nullable
    public final String adUnitName;
    /**
     * Id of ad placement.
     * Maximum length is 100 symbols. If the value exceeds this limit it will be truncated by AppMetrica.
     */
    @Nullable
    public final String adPlacementId;
    /**
     * Name of ad placement.
     * Maximum length is 100 symbols. If the value exceeds this limit it will be truncated by AppMetrica.
     */
    @Nullable
    public final String adPlacementName;
    /**
     * Precision.
     * Example: "publisher_defined", "estimated".
     * Maximum length is 100 symbols. If the value exceeds this limit it will be truncated by AppMetrica.
     */
    @Nullable
    public final String precision;
    /**
     * Arbitrary payload: additional info represented as key-value pairs.
     * Maximum size is 30 KB. If the value exceeds this limit it will be truncated by AppMetrica.
     */
    @Nullable
    public final Map<String, String> payload;

    private AdRevenue(@NonNull BigDecimal adRevenue,
                      @NonNull Currency currency,
                      @Nullable AdType adType,
                      @Nullable String adNetwork,
                      @Nullable String adUnitId,
                      @Nullable String adUnitName,
                      @Nullable String adPlacementId,
                      @Nullable String adPlacementName,
                      @Nullable String precision,
                      @Nullable Map<String, String> payload) {
        this.adRevenue = adRevenue;
        this.currency = currency;
        this.adType = adType;
        this.adNetwork = adNetwork;
        this.adUnitId = adUnitId;
        this.adUnitName = adUnitName;
        this.adPlacementId = adPlacementId;
        this.adPlacementName = adPlacementName;
        this.precision = precision;
        this.payload = payload == null ? null : CollectionUtils.unmodifiableMapCopy(payload);
    }

    /**
     * Creates the new instance of {@link Builder}.
     *
     * @param adRevenue Amount of money received via ad revenue.
     * @param currency Currency.
     * @return new {@link Builder} instance.
     *
     * @see AdRevenue#adRevenue
     * @see AdRevenue#currency
     */
    public static Builder newBuilder(@NonNull BigDecimal adRevenue, @NonNull Currency currency) {
        return new Builder(adRevenue, currency);
    }

    /**
     * Creates the new instance of {@link Builder}.
     *
     * @param adRevenueMicros Amount of money received via ad revenue represented as micros
     *                        (actual value multiplied by 10^6).
     *                        It will be converted to {@link BigDecimal}.
     * @param currency Currency.
     * @return new {@link Builder} instance.
     *
     * @see AdRevenue#adRevenue
     * @see AdRevenue#currency
     */
    public static Builder newBuilder(long adRevenueMicros, @NonNull Currency currency) {
        return new Builder(Utils.microsToBigDecimal(adRevenueMicros), currency);
    }

    /**
     * Creates the new instance of {@link Builder}.
     *
     * @param adRevenue Amount of money received via ad revenue represented as double.
     *                  It will be converted to {@link BigDecimal}.
     *                  Note that for that purpose AppMetrica uses {@link BigDecimal#BigDecimal(double)}} constructor
     *                  which can yield unpredictable results for values that cannot be precisely represented as double.
     * @param currency Currency.
     * @return new {@link Builder} instance.
     *
     * @see AdRevenue#adRevenue
     * @see AdRevenue#currency
     */
    public static Builder newBuilder(double adRevenue, @NonNull Currency currency) {
        return new Builder(new BigDecimal(Utils.getFiniteDoubleOrDefault(adRevenue, 0d)), currency);
    }

    /**
     * Builder class for {@link AdRevenue} objects.
     */
    public static class Builder {

        @NonNull
        private final BigDecimal adRevenue;
        @NonNull
        private final Currency currency;
        @Nullable
        private AdType adType;
        @Nullable
        private String adNetwork;
        @Nullable
        private String adUnitId;
        @Nullable
        private String adUnitName;
        @Nullable
        private String adPlacementId;
        @Nullable
        private String adPlacementName;
        @Nullable
        private String precision;
        @Nullable
        private Map<String, String> payload;

        private Builder(@NonNull BigDecimal adRevenue, @NonNull Currency currency) {
            this.adRevenue = adRevenue;
            this.currency = currency;
        }

        /**
         * Sets ad type.
         *
         * @param adType ad type.
         * @return same {@link Builder} object.
         *
         * @see AdRevenue#adType
         */
        public Builder withAdType(@Nullable AdType adType) {
            this.adType = adType;
            return this;
        }

        /**
         * Sets ad network.
         *
         * @param adNetwork ad network.
         * @return same {@link Builder} object.
         *
         * @see AdRevenue#adNetwork
         */
        public Builder withAdNetwork(@Nullable String adNetwork) {
            this.adNetwork = adNetwork;
            return this;
        }

        /**
         * Sets ad unit id.
         *
         * @param adUnitId Id of ad unit.
         * @return same {@link Builder} object.
         *
         * @see AdRevenue#adUnitId
         */
        public Builder withAdUnitId(@Nullable String adUnitId) {
            this.adUnitId = adUnitId;
            return this;
        }

        /**
         * Sets ad unit name.
         *
         * @param adUnitName Name of ad unit.
         * @return same {@link Builder} object.
         *
         * @see AdRevenue#adUnitName
         */
        public Builder withAdUnitName(@Nullable String adUnitName) {
            this.adUnitName = adUnitName;
            return this;
        }

        /**
         * Sets ad placement id.
         *
         * @param adPlacementId Id of ad placement.
         * @return same {@link Builder} object.
         *
         * @see AdRevenue#adPlacementId
         */
        public Builder withAdPlacementId(@Nullable String adPlacementId) {
            this.adPlacementId = adPlacementId;
            return this;
        }

        /**
         * Sets ad placement name.
         *
         * @param adPlacementName Name of ad placement.
         * @return same {@link Builder} object.
         *
         * @see AdRevenue#adPlacementName
         */
        public Builder withAdPlacementName(@Nullable String adPlacementName) {
            this.adPlacementName = adPlacementName;
            return this;
        }

        /**
         * Sets precision.
         *
         * @param precision Precision
         * @return same {@link Builder} object.
         *
         * @see AdRevenue#precision
         */
        public Builder withPrecision(@Nullable String precision) {
            this.precision = precision;
            return this;
        }

        /**
         * Sets payload.
         *
         * @param payload Arbitrary payload: additional info represented as key-value pairs.
         * @return same {@link Builder} object.
         *
         * @see AdRevenue#payload
         */
        public Builder withPayload(@Nullable Map<String, String> payload) {
            this.payload = payload == null ? null : CollectionUtils.copyOf(payload);
            return this;
        }

        /**
         * Constructs {@link AdRevenue} object.
         *
         * @return constructed {@link AdRevenue} object.
         */
        public AdRevenue build() {
            return new AdRevenue(
                    adRevenue,
                    currency,
                    adType,
                    adNetwork,
                    adUnitId,
                    adUnitName,
                    adPlacementId,
                    adPlacementName,
                    precision,
                    payload
            );
        }
    }
}
