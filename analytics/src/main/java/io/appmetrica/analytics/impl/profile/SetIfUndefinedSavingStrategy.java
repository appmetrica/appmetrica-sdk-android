package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;

public class SetIfUndefinedSavingStrategy extends BaseSavingStrategy {

    public SetIfUndefinedSavingStrategy(@NonNull AttributeSaver saver) {
        super(saver);
    }

    @Override
    public Userprofile.Profile.Attribute save(@NonNull UserProfileStorage storage,
                                              @Nullable Userprofile.Profile.Attribute existing,
                                              @NonNull AttributeFactory substitute) {
        if (isAttributeInvalid(existing)) {
            Userprofile.Profile.Attribute attribute = substitute.createAttribute();
            attribute.metaInfo.setIfUndefined = true;
            return getSaver().save(storage, attribute);
        }
        return null;
    }
}
