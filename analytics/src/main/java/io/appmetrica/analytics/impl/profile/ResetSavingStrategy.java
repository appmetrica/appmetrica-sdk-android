package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;

public class ResetSavingStrategy extends BaseSavingStrategy {

    ResetSavingStrategy(@NonNull AttributeSaver saver) {
        super(saver);
    }

    @Override
    public Userprofile.Profile.Attribute save(@NonNull UserProfileStorage storage,
                                              @Nullable Userprofile.Profile.Attribute existing,
                                              @NonNull AttributeFactory substitute) {
        Userprofile.Profile.Attribute attribute = substitute.createAttribute();
        attribute.metaInfo.reset = true;
        return getSaver().save(storage, attribute);
    }
}
