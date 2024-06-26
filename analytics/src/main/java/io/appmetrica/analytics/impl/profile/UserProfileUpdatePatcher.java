package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;

public interface UserProfileUpdatePatcher {

    void apply(@NonNull UserProfileStorage userProfileStorage);

    void setPublicLogger(@NonNull PublicLogger publicLogger);

}
