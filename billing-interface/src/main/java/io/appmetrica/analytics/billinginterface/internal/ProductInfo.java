package io.appmetrica.analytics.billinginterface.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ProductInfo {

    @NonNull
    public final ProductType type;
    @NonNull
    public final String sku;
    public final int quantity;
    public final long priceMicros;
    @NonNull
    public final String priceCurrency;
    public final long introductoryPriceMicros;
    @Nullable
    public final Period introductoryPricePeriod;
    public final int introductoryPriceCycles;
    @Nullable
    public final Period subscriptionPeriod;
    @NonNull
    public final String signature;
    @NonNull
    public final String purchaseToken;
    public final long purchaseTime;
    public final boolean autoRenewing;
    @NonNull
    public final String purchaseOriginalJson;

    public ProductInfo(@NonNull final ProductType type,
                       @NonNull final String sku,
                       final int quantity,
                       final long priceMicros,
                       @NonNull final String priceCurrency,
                       final long introductoryPriceMicros,
                       @Nullable final Period introductoryPricePeriod,
                       final int introductoryPriceCycles,
                       @Nullable final Period subscriptionPeriod,
                       @NonNull final String signature,
                       @NonNull final String purchaseToken,
                       final long purchaseTime,
                       final boolean autoRenewing,
                       @NonNull final String purchaseOriginalJson) {
        this.type = type;
        this.sku = sku;
        this.quantity = quantity;
        this.priceMicros = priceMicros;
        this.priceCurrency = priceCurrency;
        this.introductoryPriceMicros = introductoryPriceMicros;
        this.introductoryPricePeriod = introductoryPricePeriod;
        this.introductoryPriceCycles = introductoryPriceCycles;
        this.subscriptionPeriod = subscriptionPeriod;
        this.signature = signature;
        this.purchaseToken = purchaseToken;
        this.purchaseTime = purchaseTime;
        this.autoRenewing = autoRenewing;
        this.purchaseOriginalJson = purchaseOriginalJson;
    }

    @Override
    @NonNull
    public String toString() {
        return "ProductInfo{" +
                "type=" + type +
                ", sku='" + sku + '\'' +
                ", quantity=" + quantity +
                ", priceMicros=" + priceMicros +
                ", priceCurrency='" + priceCurrency + '\'' +
                ", introductoryPriceMicros=" + introductoryPriceMicros +
                ", introductoryPricePeriod=" + introductoryPricePeriod +
                ", introductoryPriceCycles=" + introductoryPriceCycles +
                ", subscriptionPeriod=" + subscriptionPeriod +
                ", signature='" + signature + '\'' +
                ", purchaseToken='" + purchaseToken + '\'' +
                ", purchaseTime=" + purchaseTime +
                ", autoRenewing=" + autoRenewing +
                ", purchaseOriginalJson='" + purchaseOriginalJson + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductInfo that = (ProductInfo) o;

        if (quantity != that.quantity) return false;
        if (priceMicros != that.priceMicros) return false;
        if (introductoryPriceMicros != that.introductoryPriceMicros) return false;
        if (introductoryPriceCycles != that.introductoryPriceCycles) return false;
        if (purchaseTime != that.purchaseTime) return false;
        if (autoRenewing != that.autoRenewing) return false;
        if (type != that.type) return false;
        if (!sku.equals(that.sku)) return false;
        if (!priceCurrency.equals(that.priceCurrency)) return false;
        if (introductoryPricePeriod != null ?
                !introductoryPricePeriod.equals(that.introductoryPricePeriod) : that.introductoryPricePeriod != null)
            return false;
        if (subscriptionPeriod != null ?
                !subscriptionPeriod.equals(that.subscriptionPeriod) : that.subscriptionPeriod != null)
            return false;
        if (!signature.equals(that.signature)) return false;
        if (!purchaseToken.equals(that.purchaseToken)) return false;
        return purchaseOriginalJson.equals(that.purchaseOriginalJson);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + sku.hashCode();
        result = 31 * result + quantity;
        result = 31 * result + (int) (priceMicros ^ (priceMicros >>> 32));
        result = 31 * result + priceCurrency.hashCode();
        result = 31 * result + (int) (introductoryPriceMicros ^ (introductoryPriceMicros >>> 32));
        result = 31 * result + (introductoryPricePeriod != null ? introductoryPricePeriod.hashCode() : 0);
        result = 31 * result + introductoryPriceCycles;
        result = 31 * result + (subscriptionPeriod != null ? subscriptionPeriod.hashCode() : 0);
        result = 31 * result + signature.hashCode();
        result = 31 * result + purchaseToken.hashCode();
        result = 31 * result + (int) (purchaseTime ^ (purchaseTime >>> 32));
        result = 31 * result + (autoRenewing ? 1 : 0);
        result = 31 * result + purchaseOriginalJson.hashCode();
        return result;
    }
}
