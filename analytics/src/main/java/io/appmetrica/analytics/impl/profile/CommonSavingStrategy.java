package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;

public class CommonSavingStrategy extends BaseSavingStrategy {

    public CommonSavingStrategy(@NonNull AttributeSaver saver) {
        super(saver);
    }

    @Override
    public Userprofile.Profile.Attribute save(@NonNull UserProfileStorage storage,
                                              @Nullable Userprofile.Profile.Attribute existing,
                                              @NonNull AttributeFactory substitute) {
        if (isAttributeInvalid(existing)) {
            return getSaver().save(storage, substitute.createAttribute());
        } else {
            existing.metaInfo = new Userprofile.Profile.AttributeMetaInfo();
            return existing;
        }
    }
}
