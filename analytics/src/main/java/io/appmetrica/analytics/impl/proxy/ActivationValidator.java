package io.appmetrica.analytics.impl.proxy;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator;
import io.appmetrica.analytics.impl.utils.validation.ValidationResult;

public class ActivationValidator extends ThrowIfFailedValidator<Void> {

    public ActivationValidator(@NonNull final AppMetricaFacadeProvider provider) {
        super(new SilentActivationValidator(provider));
    }

    public ValidationResult validate() {
        return super.validate(null);
    }
}
