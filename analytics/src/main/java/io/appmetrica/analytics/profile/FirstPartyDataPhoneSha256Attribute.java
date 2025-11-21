package io.appmetrica.analytics.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.profile.CommonSavingStrategy;
import io.appmetrica.analytics.impl.profile.Constants;
import io.appmetrica.analytics.impl.profile.CustomAttribute;
import io.appmetrica.analytics.impl.profile.LimitedSaver;
import io.appmetrica.analytics.impl.profile.StringSetUpdatePatcher;
import io.appmetrica.analytics.impl.profile.UserProfileUpdatePatcher;
import io.appmetrica.analytics.impl.profile.fpd.Sha256Converter;
import io.appmetrica.analytics.impl.utils.limitation.CollectionLimitation;
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor;
import io.appmetrica.analytics.impl.utils.limitation.StringTrimmer;
import io.appmetrica.analytics.impl.utils.validation.DummyValidator;
import java.util.Arrays;

/**
 * Attribute for setting the user's phone number hash.
 * <b>The phone values will be normalized and hashed using SHA-256 before sending.</b>
 * Supports setting multiple phone values (up to 10).
 */
public class FirstPartyDataPhoneSha256Attribute {

    private static final int LIMIT = 10;

    @NonNull
    private final CustomAttribute mCustomAttribute;
    @NonNull
    private final Sha256Converter hashConverter;

    FirstPartyDataPhoneSha256Attribute(
        @NonNull Sha256Converter hashConverter
    ) {
        mCustomAttribute = new CustomAttribute(
            Constants.FIRST_PARTY_DATA_PREFIX + "phone_sha256",
            new DummyValidator<>(),
            new LimitedSaver(
                new CollectionLimitation(EventLimitationProcessor.USER_PROFILE_CUSTOM_ATTRIBUTE_MAX_COUNT)
            )
        );
        this.hashConverter = hashConverter;
    }

    /**
     * Sets multiple phone values for the attribute.
     * <b>The values will be normalized and hashed using SHA-256 before sending.</b>
     * Maximum number of values is limited to 10.
     * Drops any values above the limit.
     *
     * @param values the phone values to set
     * @return the user profile update object
     */
    @NonNull
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withPhoneValues(@NonNull String... values) {
        return withPhoneValues(Arrays.asList(values));
    }

    /**
     * Sets multiple phone values for the attribute from an iterable.
     * <b>The values will be normalized and hashed using SHA-256 before sending.</b>
     * Maximum number of values is limited to 10.
     * Drops any values above the limit.
     *
     * @param values the phone values to set
     * @return the user profile update object
     */
    @NonNull
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withPhoneValues(@NonNull Iterable<String> values) {
        return new UserProfileUpdate<>(
            new StringSetUpdatePatcher(
                mCustomAttribute.getKey(),
                hashConverter.convert(values),
                LIMIT,
                new StringTrimmer(
                    EventLimitationProcessor.USER_PROFILE_CUSTOM_ATTRIBUTE_KEY_MAX_LENGTH,
                    "First party data phones attribute"
                ),
                mCustomAttribute.getKeyValidator(),
                new CommonSavingStrategy(mCustomAttribute.getSaver())
            )
        );
    }
}
