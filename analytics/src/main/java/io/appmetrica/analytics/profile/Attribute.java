package io.appmetrica.analytics.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.profile.KeyValidator;
import io.appmetrica.analytics.impl.profile.LimitedSaver;
import io.appmetrica.analytics.impl.profile.fpd.EmailNormalizer;
import io.appmetrica.analytics.impl.profile.fpd.PhoneNormalizer;
import io.appmetrica.analytics.impl.profile.fpd.Sha256Converter;
import io.appmetrica.analytics.impl.profile.fpd.TelegramLoginNormalizer;
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
public final class Attribute {
    private Attribute() {}

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

    /**
     * Creates an attribute for setting the user's phone number hash.
     * <b>The value will be normalized and hashed using SHA-256 before sending.</b>
     *
     * @return The {@link io.appmetrica.analytics.profile.FirstPartyDataPhoneSha256Attribute} object
     */
    @NonNull
    public static FirstPartyDataPhoneSha256Attribute phoneHash() {
        return new FirstPartyDataPhoneSha256Attribute(
            new Sha256Converter(new PhoneNormalizer())
        );
    }

    /**
     * Creates an attribute for setting the user's email address hash.
     * <b>The value will be normalized and hashed using SHA-256 before sending.</b>
     *
     * @return The {@link io.appmetrica.analytics.profile.FirstPartyDataEmailSha256Attribute} object
     */
    @NonNull
    public static FirstPartyDataEmailSha256Attribute emailHash() {
        return new FirstPartyDataEmailSha256Attribute(
            new Sha256Converter(new EmailNormalizer())
        );
    }

    /**
     * Creates an attribute for setting the user's Telegram login hash.
     * <b>The value will be normalized and hashed using SHA-256 before sending.</b>
     *
     * @return The {@link io.appmetrica.analytics.profile.FirstPartyDataTelegramLoginSha256Attribute} object
     */
    @NonNull
    public static FirstPartyDataTelegramLoginSha256Attribute telegramLoginHash() {
        return new FirstPartyDataTelegramLoginSha256Attribute(
            new Sha256Converter(new TelegramLoginNormalizer())
        );
    }
}
