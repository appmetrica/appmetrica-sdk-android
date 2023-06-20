package io.appmetrica.analytics.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.profile.KeyValidator;
import io.appmetrica.analytics.impl.profile.LimitedSaver;
import io.appmetrica.analytics.impl.utils.limitation.CollectionLimitation;
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor;
import io.appmetrica.analytics.impl.utils.limitation.StringTrimmer;

/**
 * The attribute class.
 * <p>Attribute is a property of the user profile.
 * You can use predefined profiles (e.g. name, gender, etc.) or create your own.</p>
 * <p>AppMetrica allows you to create up to 100 custom attributes.</p>
 *
 * Attributes are applied by using the
 * {@link io.appmetrica.analytics.profile.UserProfile.Builder#apply(UserProfileUpdate)} method.
 */
public class Attribute {

    /**
     * Creates a custom string attribute.
     *
     * @param key Attribute key. It can contain up to 200 characters
     *
     * @return The {@link io.appmetrica.analytics.profile.StringAttribute} object
     */
    @NonNull
    public static StringAttribute customString(@NonNull String key) {
        return new StringAttribute(
                key,
                new StringTrimmer(
                        EventLimitationProcessor.USER_PROFILE_STRING_ATTRIBUTE_MAX_LENGTH,
                        "String attribute \"" + key + "\""
                ),
                new KeyValidator(),
                new LimitedSaver(
                        new CollectionLimitation(EventLimitationProcessor.USER_PROFILE_CUSTOM_ATTRIBUTE_MAX_COUNT)
                )
        );
    }

    /**
     * Creates a custom number attribute.
     *
     * @param key Attribute key. It can contain up to 200 characters
     *
     * @return The {@link io.appmetrica.analytics.profile.NumberAttribute} object
     */
    @NonNull
    public static NumberAttribute customNumber(@NonNull String key) {
        return new NumberAttribute(key, new KeyValidator(), new LimitedSaver(
                new CollectionLimitation(EventLimitationProcessor.USER_PROFILE_CUSTOM_ATTRIBUTE_MAX_COUNT)
        ));
    }

    /**
     * Creates a custom boolean attribute.
     *
     * @param key Attribute key. It can contain up to 200 characters
     *
     * @return The {@link io.appmetrica.analytics.profile.BooleanAttribute} object
     */
    @NonNull
    public static BooleanAttribute customBoolean(@NonNull String key) {
        return new BooleanAttribute(key, new KeyValidator(), new LimitedSaver(
                new CollectionLimitation(EventLimitationProcessor.USER_PROFILE_CUSTOM_ATTRIBUTE_MAX_COUNT)
        ));
    }

    /**
     * Creates a custom counter attribute.
     *
     * @param key Attribute key. It can contain up to 200 characters
     *
     * @return The {@link io.appmetrica.analytics.profile.CounterAttribute} object
     */
    @NonNull
    public static CounterAttribute customCounter(@NonNull String key) {
        return new CounterAttribute(key, new KeyValidator(), new LimitedSaver(
                new CollectionLimitation(EventLimitationProcessor.USER_PROFILE_CUSTOM_ATTRIBUTE_MAX_COUNT)
        ));
    }

    /**
     * Creates a gender attribute.
     *
     * @return The {@link io.appmetrica.analytics.profile.GenderAttribute} object
     */
    @NonNull
    public static GenderAttribute gender() {
        return new GenderAttribute();
    }

    /**
     * Creates a birth date attribute.
     *
     * @return The {@link io.appmetrica.analytics.profile.BirthDateAttribute} object
     */
    @NonNull
    public static BirthDateAttribute birthDate() {
        return new BirthDateAttribute();
    }

    /**
     * Creates a NotificationsEnabled attribute.
     * It indicates whether the user has enabled notifications for the application.
     *
     * @return The {@link io.appmetrica.analytics.profile.NotificationsEnabledAttribute} object
     */
    @NonNull
    public static NotificationsEnabledAttribute notificationsEnabled() {
        return new NotificationsEnabledAttribute();
    }

    /**
     * Creates a name attribute.
     *
     * @return The {@link io.appmetrica.analytics.profile.NameAttribute} object
     */
    @NonNull
    public static NameAttribute name() {
        return new NameAttribute();
    }
}
