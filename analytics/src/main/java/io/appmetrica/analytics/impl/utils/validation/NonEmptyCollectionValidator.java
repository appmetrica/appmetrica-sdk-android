package io.appmetrica.analytics.impl.utils.validation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.Utils;
import java.util.Collection;

public class NonEmptyCollectionValidator<T> implements Validator<Collection<T>> {

    @NonNull
    private final String objectDescription;

    public NonEmptyCollectionValidator(@NonNull String objectDescription) {
        this.objectDescription = objectDescription;
    }

    @Override
    public ValidationResult validate(@Nullable Collection<T> data) {
        if (Utils.isNullOrEmpty(data)) {
            return ValidationResult.failed(this, objectDescription + " is null or empty.");
        } else {
            return ValidationResult.successful(this);
        }
    }
}
