package io.appmetrica.analytics.profile;

import io.appmetrica.analytics.impl.profile.Constants;
import io.appmetrica.analytics.impl.profile.SimpleSaver;
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor;
import io.appmetrica.analytics.impl.utils.limitation.StringTrimmer;
import io.appmetrica.analytics.impl.utils.validation.DummyValidator;

/**
 * The name attribute class.
 * It enables setting user name for the profile.
 *
 * <p><b>NOTE:</b> The maximum length of the user profile name is 100 characters.</p>
 *
 * <p><b>EXAMPLE:</b></p>
 * <pre>
 *     {@code UserProfile userProfile = new UserProfile.Builder()
 *                     .apply(Attribute.name().withValue("John"))
 *                     .build();}
 * </pre>
 */
public class NameAttribute extends StringAttribute {

    NameAttribute() {
        super(
                Constants.APPMETRICA_PREFIX + "name",
                new StringTrimmer(EventLimitationProcessor.USER_PROFILE_NAME_MAX_LENGTH,
                        "Name attribute"),
                new DummyValidator<String>(),
                new SimpleSaver()
        );
    }
}
