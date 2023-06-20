package io.appmetrica.analytics.impl.utils.validation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.ValidationException;

public class ThrowIfFailedValidator<T> implements Validator<T> {

    @NonNull
    private final Validator<T> mInternalValidator;

    public ThrowIfFailedValidator(@NonNull Validator<T> internalValidator) {
        mInternalValidator = internalValidator;
    }

    @Override
    public ValidationResult validate(@Nullable T data) {
        ValidationResult result = mInternalValidator.validate(data);
        if (result.isValid()) {
            return result;
        } else {
            throw new ValidationException(result.getDescription());
        }
    }

    @NonNull
    @VisibleForTesting
    public Validator<T> getInternalValidator() {
        return mInternalValidator;
    }
}
