package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.utils.limitation.CollectionLimitation;

public class LimitedSaver implements AttributeSaver {

    @NonNull
    private final CollectionLimitation mLimitation;

    public LimitedSaver(@NonNull CollectionLimitation limitation) {
        mLimitation = limitation;
    }

    @Nullable
    @Override
    public Userprofile.Profile.Attribute save(@NonNull UserProfileStorage storage,
                                              @NonNull Userprofile.Profile.Attribute existing) {
        if (storage.getLimitedAttributeCount() == mLimitation.getMaxSize()) {
            if (storage.get(existing.type, new String(existing.name)) != null) {
                storage.put(existing);
            }
        } else if (storage.getLimitedAttributeCount() < mLimitation.getMaxSize()) {
            storage.put(existing);
            storage.incrementLimitedAttributeCount();
        }
        return existing;
    }
}
