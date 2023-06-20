package io.appmetrica.analytics.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.profile.AttributeSaver;
import io.appmetrica.analytics.impl.profile.CommonSavingStrategy;
import io.appmetrica.analytics.impl.profile.CustomAttribute;
import io.appmetrica.analytics.impl.profile.ResetUpdatePatcher;
import io.appmetrica.analytics.impl.profile.SetIfUndefinedSavingStrategy;
import io.appmetrica.analytics.impl.profile.StringUpdatePatcher;
import io.appmetrica.analytics.impl.profile.UserProfileUpdatePatcher;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.utils.limitation.Trimmer;
import io.appmetrica.analytics.impl.utils.validation.Validator;

/**
 * The string attribute class.
 * It enables creating custom string attribute for the user profile.
 *
 * <p><b>EXAMPLE:</b></p>
 * <pre>
 *     {@code UserProfile userProfile = new UserProfile.Builder()
 *                     .apply(Attribute.customString("favorite_country").withValue("Russia"))
 *                     .build();}
 * </pre>
 */
public class StringAttribute {

    private final Trimmer<String> mValueTrimmer;
    private final CustomAttribute mCustomAttribute;

    StringAttribute(@NonNull String key,
                    @NonNull Trimmer<String> trimmer,
                    @NonNull Validator<String> keyValidator,
                    @NonNull AttributeSaver saver) {
        mCustomAttribute = new CustomAttribute(key, keyValidator, saver);
        mValueTrimmer = trimmer;
    }

    /**
     * Updates the string attribute with the specified value.
     *
     * @param value String value
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    @NonNull
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withValue(@NonNull String value) {
        return new UserProfileUpdate<StringUpdatePatcher>(
                new StringUpdatePatcher(
                        mCustomAttribute.getKey(),
                        value,
                        mValueTrimmer,
                        mCustomAttribute.getKeyValidator(),
                        new CommonSavingStrategy(mCustomAttribute.getSaver())
                )
        );
    }

    /**
     * Updates the attribute with the specified value only if the attribute value is undefined.
     * The method doesn't affect the value if it has been set earlier.
     *
     * @param value String value
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    @NonNull
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withValueIfUndefined(@NonNull String value) {
        return new UserProfileUpdate<StringUpdatePatcher>(
                new StringUpdatePatcher(
                        mCustomAttribute.getKey(),
                        value,
                        mValueTrimmer,
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
                        Userprofile.Profile.Attribute.STRING,
                        mCustomAttribute.getKey(),
                        mCustomAttribute.getKeyValidator(),
                        mCustomAttribute.getSaver()
                )
        );
    }

}
