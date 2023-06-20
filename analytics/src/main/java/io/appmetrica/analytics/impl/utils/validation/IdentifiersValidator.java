package io.appmetrica.analytics.impl.utils.validation;

import androidx.annotation.NonNull;
import java.util.HashSet;
import java.util.List;

public class IdentifiersValidator implements Validator<List<String>> {

    @NonNull
    private final String mObjectDescription;
    @NonNull
    private final HashSet<String> mValidIdentifiers;

    public IdentifiersValidator(@NonNull String objectDescription, @NonNull HashSet<String> validIdentifiers) {
        mObjectDescription = objectDescription;
        mValidIdentifiers = validIdentifiers;
    }

    @Override
    public ValidationResult validate(@NonNull List<String> identifiers) {
        for (String identifier : identifiers) {
            if (mValidIdentifiers.contains(identifier) == false) {
                return ValidationResult.failed(this,
                        mObjectDescription + " contains invalid identifier: " + identifier);
            }
        }
        return ValidationResult.successful(this);
    }

    @NonNull
    public String getObjectDescription() {
        return mObjectDescription;
    }
}
