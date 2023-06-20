package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.utils.limitation.Trimmer;
import io.appmetrica.analytics.impl.utils.validation.Validator;

public class StringUpdatePatcher extends CommonUserProfileUpdatePatcher<String> {

    private final Trimmer<String> mValueTrimmer;

    public StringUpdatePatcher(@NonNull String key,
                               @NonNull String value,
                               @NonNull Trimmer<String> valueTrimmer,
                               @NonNull Validator<String> keyValidator,
                               @NonNull BaseSavingStrategy saver) {
        super(
                Userprofile.Profile.Attribute.STRING,
                key,
                value,
                keyValidator,
                saver
        );
        mValueTrimmer = valueTrimmer;
    }

    @Override
    protected void setValue(@NonNull Userprofile.Profile.Attribute attribute) {
        String trimmedValue = mValueTrimmer.trim(getValue());
        attribute.value.stringValue = trimmedValue == null? new byte[] {} : trimmedValue.getBytes();
    }

    @VisibleForTesting
    public Trimmer<String> getValueTrimmer() {
        return mValueTrimmer;
    }

}
