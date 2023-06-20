package io.appmetrica.analytics.impl.utils.validation;

import androidx.annotation.NonNull;

public final class ValidationResult {

    private final Class<? extends Validator> mValidatorClass;
    private final boolean mValid;
    private final String mDescription;

    private ValidationResult(@NonNull Validator<?> validator,
                             boolean valid,
                             @NonNull String description) {
        mValidatorClass = validator.getClass();
        mValid = valid;
        mDescription = description;
    }

    public final boolean isValid() {
        return mValid;
    }

    @NonNull
    public final String getDescription() {
        return mDescription;
    }

    @NonNull
    public Class<? extends Validator> getValidatorClass() {
        return mValidatorClass;
    }

    public static final ValidationResult successful(@NonNull Validator<?> validator) {
        return new ValidationResult(validator, true, "");
    }

    public static final ValidationResult failed(@NonNull Validator<?> validator, @NonNull String description) {
        return new ValidationResult(validator, false, description);
    }
}
