package io.appmetrica.analytics.ecommerce;

import androidx.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Describes a screen (page).
 */
public class ECommerceScreen {

    /** Creates a new {@link ECommerceScreen} instance. */
    public ECommerceScreen() {}

    @Nullable
    private String name;
    @Nullable
    private List<String> categoriesPath;
    @Nullable
    private String searchQuery;
    @Nullable
    private Map<String, String> payload;

    /**
     * Returns the screen name.
     *
     * @return screen name
     * @see ECommerceScreen#setName(String)
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
     * Returns the screen categories path.
     *
     * @return categories path
     * @see ECommerceScreen#setCategoriesPath(java.util.List)
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
     * Returns the search query.
     *
     * @return search query
     * @see ECommerceScreen#setSearchQuery(String)
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
     * Returns the screen payload.
     *
     * @return payload
     * @see ECommerceScreen#setPayload(java.util.Map)
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
