package io.appmetrica.analytics.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.profile.UserProfileUpdatePatcher;

/**
 * This class indicates user profile update.
 *
 * @param <T> patcher type
 */
public class UserProfileUpdate<T extends UserProfileUpdatePatcher> {

    @NonNull
    private final T mUserProfileUpdatePatcher;

    UserProfileUpdate(@NonNull T userProfileUpdatePatcher) {
        mUserProfileUpdatePatcher = userProfileUpdatePatcher;
    }

    /**
     * For internal usage.
     *
     * @return object of internal type
     */
    @NonNull
    public T getUserProfileUpdatePatcher() {
        return mUserProfileUpdatePatcher;
    }
}
