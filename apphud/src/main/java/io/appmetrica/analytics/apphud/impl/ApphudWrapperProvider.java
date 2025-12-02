package io.appmetrica.analytics.apphud.impl;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.internal.ApphudWrapper;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;

public class ApphudWrapperProvider {

    private static final String APPHUD_V2_WRAPPER_CLASS = "io.appmetrica.analytics.apphudv2.internal.ApphudV2Wrapper";
    private static final String APPHUD_V3_WRAPPER_CLASS = "io.appmetrica.analytics.apphudv3.internal.ApphudV3Wrapper";

    @NonNull
    public static ApphudWrapper getApphudWrapper() {
        ApphudWrapper wrapper = null;
        switch (ApphudVersionProvider.getApphudVersion()) {
            case APPHUD_V3:
                wrapper = ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                    APPHUD_V3_WRAPPER_CLASS,
                    ApphudWrapper.class
                );
                break;
            case APPHUD_V2:
                wrapper = ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                    APPHUD_V2_WRAPPER_CLASS,
                    ApphudWrapper.class
                );
                break;
        }
        return wrapper != null ? wrapper : new DummyApphudWrapper();
    }
}
