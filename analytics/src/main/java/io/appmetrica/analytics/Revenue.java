package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.utils.validation.NonNullValidator;
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import java.util.Currency;

/**
 * The class to store revenue data.
 * <p>It enables revenue tracking from in-app purchases and other purchases in your application.</p>
 *
 * <p>The Revenue object should be passed to the AppMetrica server by using the
 * {@link AppMetrica#reportRevenue(Revenue)} method.</p>
 * <p>Revenue events are displayed in the AppMetrica Revenue report.</p>
 */
public class Revenue {
    /**
     * Price of the products purchased in micros (price * 10^6).
     * <p>It can be negative, e.g. for refunds.</p>
     *
     * <p><b>EXAMPLE:</b> 990000 (equivalent to 0.99 in real currency)</p>
     */
    public final long priceMicros;

    /**
     * Currency of the purchase.
     */
    @NonNull
    public final Currency currency;

    /**
     * Quantity of products purchased.
     * <p>The value cannot be negative. If the value is less than 0, the purchase is ignored.</p>
     *
     * <b>NOTE: Revenue = quantity * price.</b>
     *
     * @see Builder#withQuantity(Integer)
     */
    @Nullable
    public final Integer quantity;

    /**
     * ID of the product purchased.
     *
     * <p><b>EXAMPLE:</b> com.yandex.service.299</p>
     * <b>NOTE:</b> The string value can contain up to 200 characters.
     *
     * @see Builder#withProductID(String)
     */
    @Nullable
    public final String productID;

    /**
     * Additional information to be passed about the purchase.
     * <p>It should contain the valid JSON string.</p>
     * <p>For instance, it can be used for categorizing your products.</p>
     *
     * <b>NOTE:</b> The maximum size of the value is 30 KB.
     *
     * @see Builder#withPayload(String)
     */
    @Nullable
    public final String payload;

    /**
     * Information about the in-app purchase order from Google Play.
     *
     * @see Receipt
     * @see Builder#withReceipt(io.appmetrica.analytics.Revenue.Receipt)
     */
    @Nullable
    public final Receipt receipt;

    private Revenue(@NonNull Builder builder) {
        priceMicros = builder.priceMicros;
        currency = builder.mCurrency;
        quantity = builder.mQuantity;
        productID = builder.mProductID;
        payload = builder.mPayload;
        receipt = builder.mReceipt;
    }

    /**
     * Creates the new instance of {@link Builder}.
     *
     * @param priceMicros Price of the products purchased in micros (price * 10^6)
     * @param currency Currency of the purchase
     *
     * @return The {@link Builder} object
     *
     * @see Revenue#priceMicros
     * @see Revenue#currency
     */
    @NonNull
    public static Builder newBuilder(long priceMicros, @NonNull Currency currency) {
        return new Builder(priceMicros, currency);
    }

    /**
     * Builder class for {@link Revenue} objects.
     */
    public static class Builder {

        private static final Validator<Currency> CURRENCY_VALIDATOR = new ThrowIfFailedValidator<Currency>(
                new NonNullValidator<Currency>("revenue currency")
        );

        long priceMicros;
        @NonNull
        Currency mCurrency;
        @Nullable
        Integer mQuantity;
        @Nullable
        String mProductID;
        @Nullable
        String mPayload;
        @Nullable
        Receipt mReceipt;

        private Builder(long priceMicros, @NonNull Currency currency) {
            CURRENCY_VALIDATOR.validate(currency);
            this.priceMicros = priceMicros;
            mCurrency = currency;
        }

        /**
         * Sets the quantity of products purchased.
         * <p>It is an optional value.</p>
         *
         * @param quantity Quantity of products purchased
         *
         * @return The same {@link Builder} object
         *
         * @see Revenue#quantity
         */
        @NonNull
        public Builder withQuantity(@Nullable Integer quantity) {
            mQuantity = quantity;
            return this;
        }

        /**
         * Sets the custom ID of the product purchased.
         * <p>It is an optional value.</p>
         *
         * @param productID ID of the product
         *
         * @return The {@link Builder} object
         *
         * @see Revenue#productID
         */
        @NonNull
        public Builder withProductID(@Nullable String productID) {
            mProductID = productID;
            return this;
        }

        /**
         * Sets additional information about the purchase.
         *
         * @param payload Additional info to be passed about the purchase. It should contain the valid JSON string
         *
         * @return The same {@link Builder} object
         *
         * @see Revenue#payload
         */
        @NonNull
        public Builder withPayload(@Nullable String payload) {
            mPayload = payload;
            return this;
        }

        /**
         * Sets the receipt information about the purchase.
         * <p>Used only for in-app purchases.</p>
         *
         * @param receipt Purchase receipt data
         *
         * @return The same {@link Builder} object
         *
         * @see Revenue#receipt
         * @see Receipt
         */
        @NonNull
        public Builder withReceipt(@Nullable Receipt receipt) {
            mReceipt = receipt;
            return this;
        }

        /**
         * Creates the {@link Revenue} instance.
         *
         * @return The {@link Revenue} object
         */
        @NonNull
        public Revenue build() {
            return new Revenue(this);
        }
    }

    /**
     * The class to store in-app purchases data.
     * <p>It is used for verifying Google Play purchases.</p>
     *
     * @see <a href="https://developer.android.com/google/play/billing/api.html">Android In-App Billing API</a>
     * @see Revenue#receipt
     */
    public static class Receipt {

        /**
         * Details about the in-app purchase order from Google Play.
         * <p>It should contain data from INAPP_PURCHASE_DATA.</p>
         *
         * @see <a href="https://developer.android.com/google/play/billing/billing_reference.html#getBuyIntent">
         *     The getBuyIntent() method</a>
         * @see Builder#withData(String)
         */
        @Nullable
        public final String data;

        /**
         * Signature of the in-app purchase order from Google Play.
         * <p>It should contain data from INAPP_DATA_SIGNATURE.</p>
         *
         * @see <a href="https://developer.android.com/google/play/billing/billing_reference.html#getBuyIntent">
         *     The getBuyIntent() method</a>
         * @see Builder#withSignature(String)
         */
        @Nullable
        public final String signature;

        private Receipt(@NonNull Builder builder) {
            data = builder.mData;
            signature = builder.mSignature;
        }

        /**
         * Creates the new instance of {@link Builder}.
         *
         * @return The {@link Builder} object
         */
        @NonNull
        public static Builder newBuilder() {
            return new Builder();
        }

        /**
         * Builder class for {@link Receipt} objects.
         */
        public static class Builder {

            @Nullable
            private String mData;
            @Nullable
            private String mSignature;

            private Builder() {}

            /**
             * Sets details about the in-app purchase order from Google Play.
             *
             * @param data INAPP_PURCHASE_DATA value
             *
             * @return The same {@link Builder} object
             *
             * @see <a href="https://developer.android.com/google/play/billing/billing_reference.html#getBuyIntent">
             *     The getBuyIntent() method</a>
             * @see Receipt#data
             */
            @NonNull
            public Builder withData(@Nullable String data) {
                mData = data;
                return this;
            }

            /**
             * Sets the signature of the in-app purchase order from Google Play.
             *
             * @param signature INAPP_DATA_SIGNATURE value
             *
             * @return The same {@link Builder} object
             *
             * @see <a href="https://developer.android.com/google/play/billing/billing_reference.html#getBuyIntent">
             *     The getBuyIntent() method</a>
             * @see Receipt#signature
             */
            @NonNull
            public Builder withSignature(@Nullable String signature) {
                mSignature = signature;
                return this;
            }

            /**
             * Creates the {@link Receipt} instance.
             *
             * @return The {@link Receipt} object
             */
            @NonNull
            public Receipt build() {
                return new Receipt(this);
            }
        }

    }
}
