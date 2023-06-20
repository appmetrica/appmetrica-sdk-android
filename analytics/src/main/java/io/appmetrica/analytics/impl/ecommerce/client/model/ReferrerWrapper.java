package io.appmetrica.analytics.impl.ecommerce.client.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.ecommerce.ECommerceReferrer;

public class ReferrerWrapper {

    @Nullable
    public final String type;
    @Nullable
    public final String identifier;
    @Nullable
    public final ScreenWrapper screen;

    public ReferrerWrapper(@NonNull ECommerceReferrer input) {
        this(
                input.getType(),
                input.getIdentifier(),
                input.getScreen() == null ? null : new ScreenWrapper(input.getScreen())
        );
    }

    @Override
    public String toString() {
        return "ReferrerWrapper{" +
                "type='" + type + '\'' +
                ", identifier='" + identifier + '\'' +
                ", screen=" + screen +
                '}';
    }

    @VisibleForTesting
    public ReferrerWrapper(@Nullable String type,
                           @Nullable String identifier,
                           @Nullable ScreenWrapper screen) {
        this.type = type;
        this.identifier = identifier;
        this.screen = screen;
    }
}
