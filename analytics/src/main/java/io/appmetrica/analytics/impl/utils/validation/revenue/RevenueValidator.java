package io.appmetrica.analytics.impl.utils.validation.revenue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.Revenue;
import io.appmetrica.analytics.coreutils.internal.validation.ValidationResult;
import io.appmetrica.analytics.coreutils.internal.validation.ValidationResultsChain;
import io.appmetrica.analytics.coreutils.internal.validation.Validator;
import java.util.Arrays;
import java.util.List;

public class RevenueValidator implements Validator<Revenue> {

    private final Validator<List<ValidationResult>> mValidationResultsChain;

    public RevenueValidator() {
        mValidationResultsChain = new ValidationResultsChain();
    }

    @NonNull
    @Override
    public ValidationResult validate(@Nullable Revenue data) {
        return mValidationResultsChain.validate(Arrays.asList(new QuantityValidator().validate(data.quantity)));
    }

    @VisibleForTesting
    RevenueValidator(Validator<List<ValidationResult>> stubbedChain) {
        mValidationResultsChain = stubbedChain;
    }
}
