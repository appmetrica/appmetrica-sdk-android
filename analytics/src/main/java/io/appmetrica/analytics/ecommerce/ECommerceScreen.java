package io.appmetrica.analytics.ecommerce;

import androidx.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Describes a screen (page).
 */
public class ECommerceScreen {

    @Nullable
    private String name;
    @Nullable
    private List<String> categoriesPath;
    @Nullable
    private String searchQuery;
    @Nullable
    private Map<String, String> payload;

    /**
     * @see ECommerceScreen#setName(String)
     *
     * @return screen name
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Sets screen name.
     *
     * @param name Name of the screen.
     * @return same {@link ECommerceScreen} object
     */
    public ECommerceScreen setName(@Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * @see ECommerceScreen#setCategoriesPath(java.util.List)
     *
     * @return categories path
     */
    @Nullable
    public List<String> getCategoriesPath() {
        return categoriesPath;
    }

    /**
     * Sets screen categories path.
     *
     * @param categoriesPath Path to the screen.
     * @return same {@link ECommerceScreen} object
     */
    public ECommerceScreen setCategoriesPath(@Nullable List<String> categoriesPath) {
        this.categoriesPath = categoriesPath;
        return this;
    }

    /**
     * @see ECommerceScreen#setSearchQuery(String)
     *
     * @return search query
     */
    @Nullable
    public String getSearchQuery() {
        return searchQuery;
    }

    /**
     * Sets search query.
     *
     * @param searchQuery Search query.
     * @return same {@link ECommerceScreen} object
     */
    public ECommerceScreen setSearchQuery(@Nullable String searchQuery) {
        this.searchQuery = searchQuery;
        return this;
    }

    /**
     * @see ECommerceScreen#setPayload(java.util.Map)
     *
     * @return payload
     */
    @Nullable
    public Map<String, String> getPayload() {
        return payload;
    }

    /**
     * Sets payload.
     *
     * @param payload Payload - additional key-value structured data with various content.
     * @return same {@link ECommerceScreen} object
     */
    public ECommerceScreen setPayload(@Nullable Map<String, String> payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public String toString() {
        return "ECommerceScreen{" +
                "name='" + name + '\'' +
                ", categoriesPath=" + categoriesPath +
                ", searchQuery='" + searchQuery + '\'' +
                ", payload=" + payload +
                '}';
    }

}
