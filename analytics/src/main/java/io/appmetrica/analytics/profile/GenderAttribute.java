package io.appmetrica.analytics.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.profile.CommonSavingStrategy;
import io.appmetrica.analytics.impl.profile.Constants;
import io.appmetrica.analytics.impl.profile.CustomAttribute;
import io.appmetrica.analytics.impl.profile.ResetUpdatePatcher;
import io.appmetrica.analytics.impl.profile.SetIfUndefinedSavingStrategy;
import io.appmetrica.analytics.impl.profile.SimpleSaver;
import io.appmetrica.analytics.impl.profile.StringUpdatePatcher;
import io.appmetrica.analytics.impl.profile.UserProfileUpdatePatcher;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.utils.limitation.DummyTrimmer;
import io.appmetrica.analytics.impl.utils.validation.DummyValidator;

/**
 * The gender attribute class.
 * It enables linking user gender with the profile.
 * <p>Possible values:</p>
 * <ul>
 * <li>{@link Gender#MALE}</li>
 * <li>{@link Gender#FEMALE}</li>
 * <li>{@link Gender#OTHER}</li>
 *</ul>
 *
 * <p>You can set the OTHER value to the Gender attribute and pass additional info using the custom attribute.</p>
 * <p><b>EXAMPLE:</b></p>
 * <pre>
 *     {@code
 *      UserProfile userProfile = new UserProfile.Builder()
 *                             .apply(Attribute.gender().withValue(GenderAttribute.Gender.FEMALE))
 *                             .build();}
 * </pre>
 */
public class GenderAttribute {

    private final CustomAttribute mCustomAttribute;

    GenderAttribute() {
        mCustomAttribute = new CustomAttribute(
                Constants.APPMETRICA_PREFIX + "gender",
                new DummyValidator<String>(),
                new SimpleSaver()
        );
    }

    /**
     * Updates the gender attribute with the specified value.
     * <p>It overwrites the existing value.</p>
     *
     * @param value {@link Gender} enumeration value
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    @NonNull
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withValue(@NonNull Gender value) {
        return new UserProfileUpdate<StringUpdatePatcher>(
                new StringUpdatePatcher(
                        mCustomAttribute.getKey(),
                        value.getStringValue(),
                        new DummyTrimmer<String>(),
                        mCustomAttribute.getKeyValidator(),
                        new CommonSavingStrategy(mCustomAttribute.getSaver())
                )
        );
    }

    /**
     * Updates the gender attribute with the specified value only if the attribute value is undefined.
     * The method doesn't affect the value if it has been set earlier.
     *
     * @param value {@link Gender} enumeration value
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    @NonNull
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withValueIfUndefined(@NonNull Gender value) {
        //todo (avitenko) make special patcher
        return new UserProfileUpdate<StringUpdatePatcher>(
                new StringUpdatePatcher(
                        mCustomAttribute.getKey(),
                        value.getStringValue(),
                        new DummyTrimmer<String>(),
                        mCustomAttribute.getKeyValidator(),
                        new SetIfUndefinedSavingStrategy(mCustomAttribute.getSaver())
                )
        );
    }

    /**
     * Resets the gender attribute value.
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    @NonNull
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withValueReset() {
        return new UserProfileUpdate<ResetUpdatePatcher>(
                new ResetUpdatePatcher(
                        Userprofile.Profile.Attribute.STRING, mCustomAttribute.getKey(),
                        mCustomAttribute.getKeyValidator(),
                        mCustomAttribute.getSaver()
                )
        );
    }

    /**
     * Gender enumeration.
     *
     * <p>Possible values:</p>
     * <ul>
     * <li>{@link #MALE}</li>
     * <li>{@link #FEMALE}</li>
     * <li>{@link #OTHER}</li>
     * </ul>
     */
    public enum Gender {
        MALE("M"),
        FEMALE("F"),
        OTHER("O"),
        ;

        private final String mStringValue;

        Gender(String stringValue) {
            mStringValue = stringValue;
        }

        /**
         * Returns the string value of the gender attribute.
         *
         * @return String value of the gender attribute
         */
        public String getStringValue() {
            return mStringValue;
        }
    }
}
