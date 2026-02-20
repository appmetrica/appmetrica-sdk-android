package io.appmetrica.analytics.impl.utils.validation;

import androidx.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import kotlin.collections.CollectionsKt;

public class ValidationResultsChain implements Validator<List<ValidationResult>> {

    @Override
    public ValidationResult validate(@Nullable List<ValidationResult> results) {
        LinkedList<String> errorMessages = new LinkedList<String>();
        boolean valid = true;
        for (ValidationResult result : results) {
            if (result.isValid() == false) {
                errorMessages.add(result.getDescription());
                valid = false;
            }
        }
        return valid ? ValidationResult.successful(this)
                : ValidationResult.failed(this, CollectionsKt.joinToString(errorMessages, ", ", "", "", -1, "", null));
    }
}
