package io.appmetrica.analytics.ecommerce;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Describes transition source - screen which shown screen, product card, etc.
 */
public class ECommerceReferrer {

    @Nullable
    private String type;
    @Nullable
    private String identifier;
    @Nullable
    private ECommerceScreen screen;

    /**
     * @see ECommerceReferrer#setType(String)
     *
     * @return type
     */
    @Nullable
    public String getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type Referrer type - type of object used to perform a transition.
     *             For example: "button", "banner", etc.
     * @return same {@link ECommerceReferrer} object
     */
    @NonNull
    public ECommerceReferrer setType(@Nullable String type) {
        this.type = type;
        return this;
    }

    /**
     * @see ECommerceReferrer#setIdentifier(String)} (String)
     *
     * @return identifier
     */
    @Nullable
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets identifier.
     *
     * @param identifier Referrer identifier - identifier of object used to perform a transition.
     *                   @see ECommerceReferrer#setType(String)
     * @return same {@link ECommerceReferrer} object
     */
    @NonNull
    public ECommerceReferrer setIdentifier(@Nullable String identifier) {
        this.identifier = identifier;
        return this;
    }

    /**
     * @see ECommerceReferrer#setScreen(ECommerceScreen)
     *
     * @return screen
     */
    @Nullable
    public ECommerceScreen getScreen() {
        return screen;
    }

    /**
     * Sets screen.
     *
     * @param screen Referrer screen - screen from which the transition started.
     * @return same {@link ECommerceReferrer} object
     */
    @NonNull
    public ECommerceReferrer setScreen(@Nullable ECommerceScreen screen) {
        this.screen = screen;
        return this;
    }

    @Override
    public String toString() {
        return "ECommerceReferrer{" +
                "type='" + type + '\'' +
                ", identifier='" + identifier + '\'' +
                ", screen=" + screen +
                '}';
    }

}
