package io.appmetrica.analytics.impl.utils.validation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class IntegerFromRangeValidator implements Validator<Integer> {

    @NonNull
    private final String mDescription;
    @NonNull
    private final List<Integer> mPossibleValues;

    public IntegerFromRangeValidator(@NonNull String description, @NonNull List<Integer> possibleValues) {
        mDescription = description;
        mPossibleValues = possibleValues;
    }

    @Override
    public ValidationResult validate(@Nullable Integer data) {
        ValidationResult result;
        if (data == null) {
            result = ValidationResult.failed(this,mDescription + "is null");
        } else if (mPossibleValues.contains(data) == false) {
            result = ValidationResult.failed(
                    this,
                    String.format(
                            Locale.US,
                            "%s(value = %d) not in range of possible values: %s",
                            mDescription,
                            data,
                            mPossibleValues
                    )
            );
        } else {
            result = ValidationResult.successful(this);
        }

        return result;
    }
}
