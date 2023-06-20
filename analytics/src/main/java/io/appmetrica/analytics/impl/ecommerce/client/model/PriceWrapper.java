package io.appmetrica.analytics.impl.ecommerce.client.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.ecommerce.ECommerceAmount;
import io.appmetrica.analytics.ecommerce.ECommercePrice;
import java.util.LinkedList;
import java.util.List;

public class PriceWrapper {

    @NonNull
    public final AmountWrapper fiat;
    @Nullable
    public final List<AmountWrapper> internalComponents;

    public PriceWrapper(@NonNull ECommercePrice input) {
        this(
                new AmountWrapper(input.getFiat()),
                PriceWrapper.toInternalComponents(input.getInternalComponents())
        );
    }

    @Nullable
    public static List<AmountWrapper> toInternalComponents(@Nullable List<ECommerceAmount> inputInternalComponents) {
        List<AmountWrapper> result = null;
        if (inputInternalComponents != null) {
            result = new LinkedList<AmountWrapper>();
            for (ECommerceAmount inputAmount : inputInternalComponents) {
                result.add(new AmountWrapper(inputAmount));
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "PriceWrapper{" +
                "fiat=" + fiat +
                ", internalComponents=" + internalComponents +
                '}';
    }

    @VisibleForTesting
    public PriceWrapper(@NonNull AmountWrapper fiat,
                        @Nullable List<AmountWrapper> internalComponents) {
        this.fiat = fiat;
        this.internalComponents = internalComponents;
    }
}
