package io.appmetrica.analytics.impl.utils.validation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.StringUtils;

public class NonEmptyStringValidator implements Validator<String> {

    private final String mObjectDescription;

    public NonEmptyStringValidator(@NonNull String objectDescription) {
        mObjectDescription = objectDescription;
    }

    @Override
    public ValidationResult validate(@Nullable String data) {
        if (StringUtils.isNullOrEmpty(data)) {
            return ValidationResult.failed(this, mObjectDescription + " is empty.");
        } else {
            return ValidationResult.successful(this);
        }
    }
}
