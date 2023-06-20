package io.appmetrica.analytics.impl.utils.validation;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.StringUtils;

public class ProcessNameValidator implements Validator<String> {

    @NonNull
    private final Context mContext;

    private static final String INVALID_PROCESS_NAME_ERROR_MESSAGE_PATTERN =
            "Invalid process name: %s. Format: \"%s:{PROCESS_NAME_POSTFIX}\". For example:\"%s:Metrica\"";

    public ProcessNameValidator(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public ValidationResult validate(@Nullable String data) {
        ValidationResult result;

        if (TextUtils.isEmpty(data)) {
            result = ValidationResult.failed(this, "Process name is null or empty");
        } else {
            String packageName = mContext.getPackageName();
            String packageNameFromProcessName = data.split(StringUtils.PROCESS_POSTFIX_DELIMITER)[0];
            if (packageName.equals(packageNameFromProcessName) == false) {
                result = ValidationResult.failed(
                        this,
                        String.format(
                                INVALID_PROCESS_NAME_ERROR_MESSAGE_PATTERN,
                                data,
                                packageName,
                                packageName)
                );
            } else {
                result = ValidationResult.successful(this);
            }
        }

        return result;
    }
}
