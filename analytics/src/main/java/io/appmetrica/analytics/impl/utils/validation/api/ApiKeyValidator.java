package io.appmetrica.analytics.impl.utils.validation.api;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.utils.validation.ValidationResult;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import java.util.UUID;

public class ApiKeyValidator implements Validator<String> {

    private static final String REF_TO_DOC = "Please, read official documentation how to obtain one:";
    private static final String DOC_URL =
            "https://yandex.com/dev/appmetrica/doc/mobile-sdk-dg/concepts/android-initialize.html";

    @Override
    public ValidationResult validate(@Nullable String data) {
        if (TextUtils.isEmpty(data)) {
            return ValidationResult.failed(this, "ApiKey is empty. " + REF_TO_DOC + " " + DOC_URL);
        }
        try {
            UUID.fromString(data);
            return ValidationResult.successful(this);
        } catch (Throwable e) {
            return ValidationResult.failed(
                    this,
                    "Invalid ApiKey=" + data + ". " + REF_TO_DOC + " " + DOC_URL
            );
        }
    }
}
