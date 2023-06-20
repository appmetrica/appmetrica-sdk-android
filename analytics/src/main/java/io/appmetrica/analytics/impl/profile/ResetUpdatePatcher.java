package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.utils.validation.Validator;

public class ResetUpdatePatcher extends NamedUserProfileUpdatePatcher {

    public ResetUpdatePatcher(int type,
                              @NonNull String key,
                              @NonNull Validator<String> keyValidator,
                              @NonNull AttributeSaver attributeSaver) {
        super(
                type,
                key,
                keyValidator,
                new ResetSavingStrategy(attributeSaver)
        );
    }

    @Override
    public void apply(@NonNull UserProfileStorage userProfileStorage) {
        if (validateKey()) {
            getAttributeSavingStrategy().save(userProfileStorage, null, this);
        }
    }
}
