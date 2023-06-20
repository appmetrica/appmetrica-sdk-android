package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.utils.limitation.CollectionLimitation;
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor;

public final class CounterUpdatePatcher extends CommonUserProfileUpdatePatcher<Double> {

    public CounterUpdatePatcher(@NonNull String key, double modification) {
        super(
                Userprofile.Profile.Attribute.COUNTER,
                key,
                modification,
                new KeyValidator(),
                new CommonSavingStrategy(new LimitedSaver(
                        new CollectionLimitation(EventLimitationProcessor.USER_PROFILE_CUSTOM_ATTRIBUTE_MAX_COUNT))
                )
        );
    }

    @Override
    protected void setValue(@NonNull Userprofile.Profile.Attribute attribute) {
        attribute.value.counterModification += getValue();
    }
}
