package io.appmetrica.analytics.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.profile.AttributeSaver;
import io.appmetrica.analytics.impl.profile.CounterUpdatePatcher;
import io.appmetrica.analytics.impl.profile.CustomAttribute;
import io.appmetrica.analytics.impl.profile.UserProfileUpdatePatcher;
import io.appmetrica.analytics.impl.utils.validation.Validator;

/**
 * The counter attribute class.
 * It enables creating custom counter for the user profile.
 *
 * <p><b>EXAMPLE:</b></p>
 * <pre>
 *     CounterAttribute timeLeftAttribute = Attribute.customCounter("time_left");
 *     UserProfile userProfile = new UserProfile.Builder()
 *                     .apply(Attribute.timeLeftAttribute().withDelta(-10d))
 *                     .build();
 * </pre>
 */
public final class CounterAttribute {

    private final CustomAttribute mCustomAttribute;

    CounterAttribute(@NonNull String key,
                     @NonNull Validator<String> keyValidator,
                     @NonNull AttributeSaver saver) {
        mCustomAttribute = new CustomAttribute(key, keyValidator, saver);
    }

    /**
     * Updates the counter attribute value with the specified delta value.
     *
     * @param value Delta value to change the counter attribute value
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    @NonNull
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withDelta(double value) {
        return new UserProfileUpdate<CounterUpdatePatcher>(new CounterUpdatePatcher(mCustomAttribute.getKey(), value));
    }
}
