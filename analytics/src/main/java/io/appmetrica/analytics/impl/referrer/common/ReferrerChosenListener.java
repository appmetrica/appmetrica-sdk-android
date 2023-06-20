package io.appmetrica.analytics.impl.referrer.common;

import androidx.annotation.Nullable;

public interface ReferrerChosenListener {

    void onReferrerChosen(@Nullable ReferrerInfo referrerInfo);
}
