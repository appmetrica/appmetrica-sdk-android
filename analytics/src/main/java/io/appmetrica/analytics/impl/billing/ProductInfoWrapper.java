package io.appmetrica.analytics.impl.billing;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.billinginterface.internal.Period;
import io.appmetrica.analytics.billinginterface.internal.ProductInfo;
import io.appmetrica.analytics.billinginterface.internal.ProductType;
import io.appmetrica.analytics.impl.protobuf.backend.Revenue;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import java.util.Currency;
import java.util.concurrent.TimeUnit;

public class ProductInfoWrapper {

    @NonNull
    private final ProductInfo productInfo;

    public ProductInfoWrapper(@NonNull final ProductInfo productInfo) {
        this.productInfo = productInfo;
    }

    @NonNull
    public byte[] getDataToSend() {
        final Revenue proto = getRevenue(productInfo);
        return MessageNano.toByteArray(proto);
    }

    @NonNull
    private Revenue getRevenue(@NonNull final ProductInfo productInfo) {
        final Revenue proto = new Revenue();
        proto.quantity = productInfo.quantity;
        proto.priceMicros = productInfo.priceMicros;
        proto.currency = getCurrency(productInfo.priceCurrency).getBytes();
        proto.productId = productInfo.sku.getBytes();
        proto.receipt = getReceipt(productInfo);
        proto.autoCollected = true;
        proto.guessedBuyerDevice = Revenue.THIS;
        proto.inAppType = getInAppType(productInfo.type);
        proto.transactionInfo = getTransactionInfo(productInfo);
        if (productInfo.type == ProductType.SUBS) {
            proto.subscriptionInfo = getSubscriptionInfo(productInfo);
        }
        return proto;
    }

    @NonNull
    private String getCurrency(@NonNull final String currency) {
        try {
            return Currency.getInstance(currency).getCurrencyCode();
        } catch (Throwable ignored) {
            return "";
        }
    }

    @NonNull
    private Revenue.Receipt getReceipt(@NonNull final ProductInfo productInfo) {
        final Revenue.Receipt proto = new Revenue.Receipt();
        proto.data = productInfo.purchaseOriginalJson.getBytes();
        proto.signature = productInfo.signature.getBytes();
        return proto;
    }

    private int getInAppType(@NonNull final ProductType type) {
        switch (type) {
            case INAPP: return Revenue.PURCHASE;
            case SUBS: return Revenue.SUBSCRIPTION;
            default: return Revenue.PURCHASE;
        }
    }

    @NonNull
    private Revenue.Transaction getTransactionInfo(@NonNull final ProductInfo productInfo) {
        final Revenue.Transaction proto = new Revenue.Transaction();
        proto.id = productInfo.purchaseToken.getBytes();
        proto.time = TimeUnit.MILLISECONDS.toSeconds(productInfo.purchaseTime);
        return proto;
    }

    @NonNull
    private Revenue.SubscriptionInfo getSubscriptionInfo(@NonNull final ProductInfo productInfo) {
        final Revenue.SubscriptionInfo proto = new Revenue.SubscriptionInfo();
        proto.autoRenewing = productInfo.autoRenewing;
        if (productInfo.subscriptionPeriod != null) {
            proto.subscriptionPeriod = toProtobufPeriod(productInfo.subscriptionPeriod);
        }
        proto.introductoryInfo = getIntroductory(productInfo);
        return proto;
    }

    @NonNull
    private Revenue.SubscriptionInfo.Introductory getIntroductory(@NonNull final ProductInfo productInfo) {
        final Revenue.SubscriptionInfo.Introductory proto = new Revenue.SubscriptionInfo.Introductory();
        proto.priceMicros = productInfo.introductoryPriceMicros;
        if (productInfo.introductoryPricePeriod != null) {
            proto.period = toProtobufPeriod(productInfo.introductoryPricePeriod);
        }
        proto.numberOfPeriods = productInfo.introductoryPriceCycles;
        return proto;
    }

    @NonNull
    private Revenue.SubscriptionInfo.Period toProtobufPeriod(@NonNull final Period period) {
        final Revenue.SubscriptionInfo.Period proto = new Revenue.SubscriptionInfo.Period();
        proto.number = period.number;
        proto.timeUnit = toTimeUnit(period.timeUnit);
        return proto;
    }

    private int toTimeUnit(final Period.TimeUnit period) {
        switch (period) {
            case DAY: return Revenue.SubscriptionInfo.Period.DAY;
            case WEEK: return Revenue.SubscriptionInfo.Period.WEEK;
            case MONTH: return Revenue.SubscriptionInfo.Period.MONTH;
            case YEAR: return Revenue.SubscriptionInfo.Period.YEAR;
            default: return Revenue.SubscriptionInfo.Period.TIME_UNIT_UNKNOWN;
        }
    }
}
