package io.appmetrica.analytics.impl.utils.validation.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.utils.validation.ValidationResult;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import java.util.Map;

public class ReporterKeyIsUsedValidator implements Validator<String> {

    private final Map<String, ?> mMap;

    public ReporterKeyIsUsedValidator(@NonNull Map<String, ?> map) {
        mMap = map;
    }

    @Override
    public ValidationResult validate(@Nullable String data) {
        if (mMap.containsKey(data)) {
            return ValidationResult.failed(
                    this,
                    String.format("Failed to activate AppMetrica with provided apiKey" +
                            " ApiKey %s has already been used by another reporter.", data
                    )
            );
        } else {
            return ValidationResult.successful(this);
        }
    }
}
