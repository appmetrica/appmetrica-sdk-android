package io.appmetrica.analytics.impl.utils.validation;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

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
                : ValidationResult.failed(this, TextUtils.join(", ", errorMessages));
    }
}
