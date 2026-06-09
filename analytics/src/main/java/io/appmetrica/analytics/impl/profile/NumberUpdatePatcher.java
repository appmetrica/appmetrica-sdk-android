package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.validation.Validator;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;

public class NumberUpdatePatcher extends CommonUserProfileUpdatePatcher<Double> {

    public NumberUpdatePatcher(@NonNull String key,
                               double value,
                               @NonNull Validator<String> keyValidator,
                               @NonNull BaseSavingStrategy attributeSavingStrategy) {
        super(
                Userprofile.Profile.Attribute.NUMBER,
                key,
                value,
                keyValidator,
                attributeSavingStrategy
        );
    }

    @Override
    protected void setValue(@NonNull Userprofile.Profile.Attribute attribute) {
        attribute.value.numberValue = getValue();
    }
}
