package io.appmetrica.analytics.impl.utils.validation.revenue;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.utils.validation.ValidationResult;
import io.appmetrica.analytics.impl.utils.validation.Validator;

class QuantityValidator implements Validator<Integer> {

    @Override
    public ValidationResult validate(@Nullable Integer data) {
        if (data == null || data > 0) {
            return ValidationResult.successful(this);
        } else {
            return ValidationResult.failed(this, "Invalid quantity value " + data);
        }
    }
}
