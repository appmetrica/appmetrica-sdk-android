package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;

public abstract class BaseSavingStrategy {

    @NonNull
    private final AttributeSaver mSaver;

    BaseSavingStrategy(@NonNull AttributeSaver saver) {
        mSaver = saver;
    }

    @NonNull
    AttributeSaver getSaver() {
        return mSaver;
    }

    public abstract Userprofile.Profile.Attribute save(@NonNull UserProfileStorage storage,
                                                       @Nullable Userprofile.Profile.Attribute existing,
                                                       @NonNull AttributeFactory substitute);

    final boolean isAttributeInvalid(@Nullable Userprofile.Profile.Attribute attribute) {
        return attribute == null || attribute.metaInfo.reset;
    }

}
