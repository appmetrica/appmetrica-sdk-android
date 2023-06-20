package io.appmetrica.analytics.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.profile.AttributeSaver;
import io.appmetrica.analytics.impl.profile.CommonSavingStrategy;
import io.appmetrica.analytics.impl.profile.CustomAttribute;
import io.appmetrica.analytics.impl.profile.KeyValidator;
import io.appmetrica.analytics.impl.profile.LimitedSaver;
import io.appmetrica.analytics.impl.profile.NumberUpdatePatcher;
import io.appmetrica.analytics.impl.profile.ResetUpdatePatcher;
import io.appmetrica.analytics.impl.profile.SetIfUndefinedSavingStrategy;
import io.appmetrica.analytics.impl.profile.UserProfileUpdatePatcher;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.utils.limitation.CollectionLimitation;
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor;
import io.appmetrica.analytics.impl.utils.validation.Validator;

/**
 * The number attribute class.
 * It enables creating custom number attribute for the user profile.
 *
 * <p><b>EXAMPLE:</b></p>
 * <pre>
 *     {@code UserProfile userProfile = new UserProfile.Builder()
 *                     .apply(Attribute.customNumber("level").withValue(5d))
 *                     .build();}
 * </pre>
 */
public final class NumberAttribute {

    private final CustomAttribute mCustomAttribute;

    NumberAttribute(@NonNull String key,
                    @NonNull Validator<String> keyValidator,
                    @NonNull AttributeSaver saver) {
        mCustomAttribute = new CustomAttribute(key, keyValidator, saver);
    }

    /**
     * Updates the attribute with the specified value.
     *
     * @param value Number value
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    @NonNull
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withValue(double value) {
        return new UserProfileUpdate<NumberUpdatePatcher>(
                new NumberUpdatePatcher(
                        mCustomAttribute.getKey(),
                        value,
                        new KeyValidator(),
                        new CommonSavingStrategy(new LimitedSaver(
                                new CollectionLimitation(
                                        EventLimitationProcessor.USER_PROFILE_CUSTOM_ATTRIBUTE_MAX_COUNT
                                ))
                        )
                )
        );
    }

    /**
     * Updates the attribute with the specified value only if the attribute value is undefined.
     * The method doesn't affect the value if it has been set earlier.
     *
     * @param value Number value
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    @NonNull
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withValueIfUndefined(double value) {
        return new UserProfileUpdate<NumberUpdatePatcher>(
                new NumberUpdatePatcher(
                        mCustomAttribute.getKey(),
                        value,
                        new KeyValidator(),
                        new SetIfUndefinedSavingStrategy(new LimitedSaver(
                                new CollectionLimitation(
                                        EventLimitationProcessor.USER_PROFILE_CUSTOM_ATTRIBUTE_MAX_COUNT
                                ))
                        )
                )
        );
    }

    /**
     * Resets the attribute value.
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    @NonNull
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withValueReset() {
        return new UserProfileUpdate<ResetUpdatePatcher>(
                new ResetUpdatePatcher(
                        Userprofile.Profile.Attribute.NUMBER,
                        mCustomAttribute.getKey(),
                        new KeyValidator(),
                        new LimitedSaver(
                                new CollectionLimitation(
                                        EventLimitationProcessor.USER_PROFILE_CUSTOM_ATTRIBUTE_MAX_COUNT
                                )
                        )
                )
        );
    }
}
