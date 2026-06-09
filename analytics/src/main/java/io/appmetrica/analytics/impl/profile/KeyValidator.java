package io.appmetrica.analytics.impl.profile;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.validation.ValidationResult;
import io.appmetrica.analytics.coreutils.internal.validation.Validator;
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor;

public class KeyValidator implements Validator<String> {

    @Override
    public ValidationResult validate(@Nullable String data) {
        if (data == null) {
            return ValidationResult.failed(this, "key is null");
        }
        if (data.startsWith("appmetrica")) {
            return ValidationResult.failed(this, "key starts with appmetrica");
        }
        if (data.length() > EventLimitationProcessor.USER_PROFILE_CUSTOM_ATTRIBUTE_KEY_MAX_LENGTH) {
            return ValidationResult.failed(this, "key length more then "
                    + EventLimitationProcessor.USER_PROFILE_CUSTOM_ATTRIBUTE_KEY_MAX_LENGTH + " characters");
        }
        return ValidationResult.successful(this);
    }
}
