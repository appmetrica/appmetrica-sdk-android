package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.impl.utils.validation.ValidationResult;
import io.appmetrica.analytics.impl.utils.validation.Validator;

public abstract class NamedUserProfileUpdatePatcher implements UserProfileUpdatePatcher, AttributeFactory {

    @NonNull
    private final String mKey;
    private final int mType;
    @NonNull
    private final Validator<String> mKeyValidator;

    @NonNull
    private final BaseSavingStrategy mAttributeSavingStrategy;

    @NonNull
    private PublicLogger mPublicLogger;

    NamedUserProfileUpdatePatcher(int type,
                                  @NonNull String key,
                                  @NonNull Validator<String> keyValidator,
                                  @NonNull BaseSavingStrategy attributeSavingStrategy) {
        mType = type;
        mKey = key;
        mKeyValidator = keyValidator;
        mAttributeSavingStrategy = attributeSavingStrategy;
        mPublicLogger = PublicLogger.getAnonymousInstance();
    }

    @NonNull
    public String getKey() {
        return mKey;
    }

    public int getType() {
        return mType;
    }

    @VisibleForTesting
    @NonNull
    public Validator<String> getKeyValidator() {
        return mKeyValidator;
    }

    @NonNull
    public BaseSavingStrategy getAttributeSavingStrategy() {
        return mAttributeSavingStrategy;
    }

    @NonNull
    public final Userprofile.Profile.Attribute createAttribute() {
        Userprofile.Profile.Attribute attribute = new Userprofile.Profile.Attribute();
        attribute.type = getType();
        attribute.name = getKey().getBytes();
        attribute.value = new Userprofile.Profile.AttributeValue();
        attribute.metaInfo = new Userprofile.Profile.AttributeMetaInfo();
        return attribute;
    }

    @Override
    public void setPublicLogger(@NonNull PublicLogger publicLogger) {
        mPublicLogger = publicLogger;
    }

    protected boolean validateKey() {
        ValidationResult result = mKeyValidator.validate(getKey());
        if (result.isValid()) {
            return true;
        } else {
            mPublicLogger.warning(
                "Attribute " + getKey() + " of type "
                + TypesToNameMapping.getTypeName(getType()) + " is skipped because "
                + result.getDescription()
            );
            return false;
        }
    }

}
