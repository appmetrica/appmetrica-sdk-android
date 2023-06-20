package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;

public interface AttributeSaver {

    @Nullable
    Userprofile.Profile.Attribute save(@NonNull UserProfileStorage storage,
                                       @NonNull Userprofile.Profile.Attribute existing);

}
