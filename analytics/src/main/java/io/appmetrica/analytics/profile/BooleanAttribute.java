package io.appmetrica.analytics.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.profile.AttributeSaver;
import io.appmetrica.analytics.impl.profile.BooleanUpdatePatcher;
import io.appmetrica.analytics.impl.profile.CommonSavingStrategy;
import io.appmetrica.analytics.impl.profile.CustomAttribute;
import io.appmetrica.analytics.impl.profile.ResetUpdatePatcher;
import io.appmetrica.analytics.impl.profile.SetIfUndefinedSavingStrategy;
import io.appmetrica.analytics.impl.profile.UserProfileUpdatePatcher;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.utils.validation.Validator;

/**
 * The boolean attribute class.
 * It enables creating custom boolean attribute for the user profile.
 * <p><b>EXAMPLE:</b></p>
 * <pre>
 *     {@code UserProfile userProfile = new UserProfile.Builder()
 *                     .apply(Attribute.customBoolean("is_enabled").withValue(true))
 *                     .build();}
 * </pre>
 */
public class BooleanAttribute {

    private final CustomAttribute mCustomAttribute;

    BooleanAttribute(@NonNull String key,
                     @NonNull Validator<String> keyValidator,
                     @NonNull AttributeSaver saver) {
        mCustomAttribute = new CustomAttribute(key, keyValidator, saver);
    }

    /**
     * Updates the attribute with the specified value.
     *
     * @param value Boolean value
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    @NonNull
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withValue(boolean value) {
        return new UserProfileUpdate<BooleanUpdatePatcher>(
                new BooleanUpdatePatcher(
                        mCustomAttribute.getKey(),
                        value,
                        mCustomAttribute.getKeyValidator(),
                        new CommonSavingStrategy(mCustomAttribute.getSaver())
                )
        );
    }

    /**
     * Updates the attribute with the specified value only if the attribute value is undefined.
     * The method doesn't affect the value if it has been set earlier.
     *
     * @param value Boolean value
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    @NonNull
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withValueIfUndefined(boolean value) {
        return new UserProfileUpdate<BooleanUpdatePatcher>(
                new BooleanUpdatePatcher(
                        mCustomAttribute.getKey(),
                        value,
                        mCustomAttribute.getKeyValidator(),
                        new SetIfUndefinedSavingStrategy(mCustomAttribute.getSaver())
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
                        Userprofile.Profile.Attribute.BOOL,
                        mCustomAttribute.getKey(),
                        mCustomAttribute.getKeyValidator(),
                        mCustomAttribute.getSaver()
                )
        );
    }
}
