package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.utils.validation.Validator;

public abstract class CommonUserProfileUpdatePatcher<T> extends NamedUserProfileUpdatePatcher {

    @NonNull
    private final T mValue;

    CommonUserProfileUpdatePatcher(int type,
                                   @NonNull String key,
                                   @NonNull T value,
                                   @NonNull Validator<String> keyValidator,
                                   @NonNull BaseSavingStrategy attributeSavingStrategy) {
        super(
                type,
                key,
                keyValidator,
                attributeSavingStrategy
        );
        mValue = value;
    }

    @NonNull
    public T getValue() {
        return mValue;
    }

    @Override
    public void apply(@NonNull UserProfileStorage userProfileStorage) {
        if (validateKey()) {
            Userprofile.Profile.Attribute attributeToModify = getOrCreateAttribute(userProfileStorage);
            if (attributeToModify != null) {
                setValue(attributeToModify);
            }
        }
    }

    @Nullable
    private Userprofile.Profile.Attribute getOrCreateAttribute(@NonNull UserProfileStorage userProfileStorage) {
        return getAttributeSavingStrategy().save(userProfileStorage, userProfileStorage.get(getType(), getKey()), this);
    }

    protected abstract void setValue(@NonNull Userprofile.Profile.Attribute attribute);
}
