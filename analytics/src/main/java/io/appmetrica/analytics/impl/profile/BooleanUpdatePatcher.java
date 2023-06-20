package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.utils.validation.Validator;

public class BooleanUpdatePatcher extends CommonUserProfileUpdatePatcher<Boolean> {

    public BooleanUpdatePatcher(@NonNull String key,
                                boolean value,
                                @NonNull Validator<String> keyValidator,
                                @NonNull BaseSavingStrategy attributeSavingStrategy) {
        super(
                Userprofile.Profile.Attribute.BOOL,
                key,
                value,
                keyValidator,
                attributeSavingStrategy
        );
    }

    @Override
    protected void setValue(@NonNull Userprofile.Profile.Attribute attribute) {
        attribute.value.boolValue = getValue();
    }
}
