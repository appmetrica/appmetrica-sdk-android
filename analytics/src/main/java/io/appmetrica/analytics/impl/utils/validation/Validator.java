package io.appmetrica.analytics.impl.utils.validation;

import androidx.annotation.Nullable;

public interface Validator<T> {

    ValidationResult validate(@Nullable T data);

}
