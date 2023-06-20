package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.utils.validation.Validator;

public final class CustomAttribute {

    @NonNull
    private final Validator<String> mKeyValidator;
    @NonNull
    private final AttributeSaver mSaver;

    @NonNull
    private final String mKey;

    public CustomAttribute(@NonNull String key,
                           @NonNull Validator<String> keyValidator,
                           @NonNull AttributeSaver saver) {
        mKey = key;
        mKeyValidator = keyValidator;
        mSaver = saver;
    }

    @NonNull
    public String getKey() {
        return mKey;
    }

    @NonNull
    public AttributeSaver getSaver() {
        return mSaver;
    }

    @NonNull
    public Validator<String> getKeyValidator() {
        return mKeyValidator;
    }
}
