package io.appmetrica.analytics.ecommerce;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Describes transition source - screen which shown screen, product card, etc.
 */
public class ECommerceReferrer {

    /** Creates a new {@link ECommerceReferrer} instance. */
    public ECommerceReferrer() {}

    @Nullable
    private String type;
    @Nullable
    private String identifier;
    @Nullable
    private ECommerceScreen screen;

    /**
     * Returns the referrer type.
     *
     * @return type
     * @see ECommerceReferrer#setType(String)
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
     * Returns the referrer identifier.
     *
     * @return identifier
     * @see ECommerceReferrer#setIdentifier(String)
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
     * Returns the referrer screen.
     *
     * @return screen
     * @see ECommerceReferrer#setScreen(ECommerceScreen)
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
