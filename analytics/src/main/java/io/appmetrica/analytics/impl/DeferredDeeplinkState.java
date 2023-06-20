package io.appmetrica.analytics.impl;

import androidx.annotation.Nullable;
import java.util.Map;

public class DeferredDeeplinkState {

    @Nullable
    public final Map<String, String> mParameters;
    @Nullable
    public final String mDeeplink;
    @Nullable
    public final String mUnparsedReferrer;

    public DeferredDeeplinkState(@Nullable String deeplink,
                                 @Nullable Map<String, String> parameters,
                                 @Nullable String unparsedReferrer) {
        mDeeplink = deeplink;
        mParameters = parameters;
        mUnparsedReferrer = unparsedReferrer;
    }

    @Override
    public String toString() {
        return "DeferredDeeplinkState{" +
                "mParameters=" + mParameters +
                ", mDeeplink='" + mDeeplink + '\'' +
                ", mUnparsedReferrer='" + mUnparsedReferrer + '\'' +
                '}';
    }
}
