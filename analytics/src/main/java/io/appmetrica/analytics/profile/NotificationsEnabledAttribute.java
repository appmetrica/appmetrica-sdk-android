package io.appmetrica.analytics.profile;

import io.appmetrica.analytics.impl.profile.Constants;
import io.appmetrica.analytics.impl.profile.SimpleSaver;
import io.appmetrica.analytics.impl.utils.validation.DummyValidator;

/**
 * The NotificationsEnabled attribute class.
 * It indicates whether the user has enabled notifications for the application.
 * It enables setting notification status for the user profile.
 *
 * <p><b>EXAMPLE:</b></p>
 * <pre>
 *     {@code UserProfile userProfile = new UserProfile.Builder()
 *                     .apply(Attribute.notificationEnabled().withValue(true))
 *                     .build();}
 * </pre>
 */
public class NotificationsEnabledAttribute extends BooleanAttribute {

    NotificationsEnabledAttribute() {
        super(
                Constants.APPMETRICA_PREFIX + "notifications_enabled",
                new DummyValidator<String>(),
                new SimpleSaver()
        );
    }
}
