package io.appmetrica.analytics.impl.utils.validation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NonNullValidator<T> implements Validator<T> {

    @NonNull
    private final String mObjectDescription;

    public NonNullValidator(@NonNull String objectDescription) {
        mObjectDescription = objectDescription;
    }

    @Override
    public ValidationResult validate(@Nullable T data) {
        if (data == null) {
            return ValidationResult.failed(this, mObjectDescription + " is null.");
        } else {
            return ValidationResult.successful(this);
        }
    }

    @NonNull
    public String getObjectDescription() {
        return mObjectDescription;
    }
}
