package io.appmetrica.analytics.impl.proxy.validation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.validation.ValidationResult;
import io.appmetrica.analytics.coreutils.internal.validation.Validator;
import io.appmetrica.analytics.impl.VerificationConstants;
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider;

public class SilentActivationValidator implements Validator<Void> {

    @NonNull
    private final AppMetricaFacadeProvider provider;

    public SilentActivationValidator(@NonNull AppMetricaFacadeProvider provider) {
        this.provider = provider;
    }

    @Override
    public ValidationResult validate(@Nullable Void data) {
        return provider.isActivated() ?
                ValidationResult.successful(this) :
                ValidationResult.failed(this, VerificationConstants.SDK_UNINITIALIZED_ERROR);
    }

    public ValidationResult validate() {
        return validate(null);
    }
}
