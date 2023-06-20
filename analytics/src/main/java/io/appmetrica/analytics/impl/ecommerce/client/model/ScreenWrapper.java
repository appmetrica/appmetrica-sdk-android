package io.appmetrica.analytics.impl.ecommerce.client.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.ecommerce.ECommerceScreen;
import java.util.List;
import java.util.Map;

public class ScreenWrapper {

    @Nullable
    public final String name;
    @Nullable
    public final List<String> categoriesPath;
    @Nullable
    public final String searchQuery;
    @Nullable
    public final Map<String, String> payload;

    public ScreenWrapper(@NonNull ECommerceScreen input) {
        this(
                input.getName(),
                CollectionUtils.arrayListCopyOfNullableCollection(input.getCategoriesPath()),
                input.getSearchQuery(),
                CollectionUtils.mapCopyOfNullableMap(input.getPayload())
        );
    }

    @NonNull
    @Override
    public String toString() {
        return "ScreenWrapper{" +
                "name='" + name + '\'' +
                ", categoriesPath=" + categoriesPath +
                ", searchQuery='" + searchQuery + '\'' +
                ", payload=" + payload +
                '}';
    }

    @VisibleForTesting
    public ScreenWrapper(@Nullable String name,
                         @Nullable List<String> categoriesPath,
                         @Nullable String searchQuery,
                         @Nullable Map<String, String> payload) {
        this.name = name;
        this.categoriesPath = categoriesPath;
        this.searchQuery = searchQuery;
        this.payload = payload;
    }
}
