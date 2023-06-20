package io.appmetrica.analytics.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.impl.profile.UserProfileUpdatePatcher;
import java.util.LinkedList;
import java.util.List;

/**
 * The class to store a user profile.
 * <p>User profile is a set of user attributes.
 * User profile details are displayed in the AppMetrica User profiles report.</p>
 * <p>The UserProfile object should be passed to the AppMetrica server by using the
 * {@link AppMetrica#reportUserProfile(UserProfile)} method.</p>
 *
 * AppMetrica has some predefined attributes. You can use them or create own custom attributes.
 * <p>User profiles are stored on the AppMetrica server.</p>
 *
 * <p><b>EXAMPLE:</b></p>
 * <pre>
 *     {@code
 *      UserProfile userProfile = new UserProfile.Builder()
 *                             .apply(Attribute.customString("foo_attribute").withValue("baz_value"))
 *                             .apply(Attribute.name().withName("John"))
 *                             .apply(Attribute.gender().withValue(GenderAttribute.Gender.MALE))
 *                             .apply(Attribute.notificationEnabled().withValue(false))
 *                             .build();
 *      AppMetrica.reportUserProfile(userProfile);
 *      AppMetrica.setProfileId("id_1");
 *     }
 * </pre>
 */
public class UserProfile {

    @NonNull
    private final List<UserProfileUpdate<? extends UserProfileUpdatePatcher>> mUserProfileUpdates;

    private UserProfile(@NonNull List<UserProfileUpdate<? extends UserProfileUpdatePatcher>> userProfileUpdates) {
        mUserProfileUpdates = CollectionUtils.unmodifiableListCopy(userProfileUpdates);
    }

    @NonNull
    public List<UserProfileUpdate<? extends UserProfileUpdatePatcher>> getUserProfileUpdates() {
        return mUserProfileUpdates;
    }

    /**
     * Creates the new instance of {@link Builder}.
     *
     * @return The {@link Builder} object.
     */
    @NonNull
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Builder class for {@link UserProfile} objects.
     */
    public static class Builder {

        private final LinkedList<UserProfileUpdate<? extends UserProfileUpdatePatcher>> mUserProfileUpdates =
                new LinkedList<UserProfileUpdate<? extends UserProfileUpdatePatcher>>();

        Builder() {}

        /**
         * Applies user profile update.
         * Use the {@link AppMetrica#reportUserProfile(UserProfile)} method to send updated data
         * to the AppMetrica server.
         *
         * @param userProfileUpdate The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object of the attribute
         *                          update.
         *
         * @return The same {@link Builder} object
         */
        public Builder apply(@NonNull UserProfileUpdate<? extends UserProfileUpdatePatcher> userProfileUpdate) {
            mUserProfileUpdates.add(userProfileUpdate);
            return this;
        }

        /**
         * Creates the {@link UserProfile} instance.
         *
         * @return The {@link UserProfile} object
         */
        @NonNull
        public UserProfile build() {
            return new UserProfile(mUserProfileUpdates);
        }

    }

}
