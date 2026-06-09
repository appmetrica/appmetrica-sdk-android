package io.appmetrica.analytics.impl.proxy.validation;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.validation.ValidationResult;
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider;
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator;

public class ActivationValidator extends ThrowIfFailedValidator<Void> {

    public ActivationValidator(@NonNull final AppMetricaFacadeProvider provider) {
        super(new SilentActivationValidator(provider));
    }

    public ValidationResult validate() {
        return super.validate(null);
    }
}
