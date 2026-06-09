package io.appmetrica.analytics.impl.utils.validation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.ValidationException;
import io.appmetrica.analytics.coreutils.internal.validation.ValidationResult;
import io.appmetrica.analytics.coreutils.internal.validation.Validator;

public class ThrowIfFailedValidator<T> implements Validator<T> {

    @NonNull
    private final Validator<T> mInternalValidator;

    public ThrowIfFailedValidator(@NonNull Validator<T> internalValidator) {
        mInternalValidator = internalValidator;
    }

    @NonNull
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
