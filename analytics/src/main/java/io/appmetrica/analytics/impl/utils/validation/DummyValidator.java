package io.appmetrica.analytics.impl.utils.validation;

import androidx.annotation.Nullable;

public class DummyValidator<T> implements Validator<T> {

    @Override
    public ValidationResult validate(@Nullable T data) {
        return ValidationResult.successful(this);
    }
}
