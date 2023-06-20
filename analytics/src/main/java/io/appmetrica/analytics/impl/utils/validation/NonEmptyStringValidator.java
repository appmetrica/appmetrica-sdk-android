package io.appmetrica.analytics.impl.utils.validation;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NonEmptyStringValidator implements Validator<String> {

    private final String mObjectDescription;

    public NonEmptyStringValidator(@NonNull String objectDescription) {
        mObjectDescription = objectDescription;
    }

    @Override
    public ValidationResult validate(@Nullable String data) {
        if (TextUtils.isEmpty(data)) {
            return ValidationResult.failed(this, mObjectDescription + " is empty.");
        } else {
            return ValidationResult.successful(this);
        }
    }
}
