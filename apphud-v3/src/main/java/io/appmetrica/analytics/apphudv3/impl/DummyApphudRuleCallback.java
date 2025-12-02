package io.appmetrica.analytics.apphudv3.impl;

import androidx.annotation.NonNull;
import com.apphud.sdk.ApphudPurchaseResult;
import com.apphud.sdk.ApphudRuleCallback;
import com.apphud.sdk.internal.domain.model.Rule;

public class DummyApphudRuleCallback implements ApphudRuleCallback {

    @Override
    public boolean shouldPerformRule(@NonNull Rule rule) {
        return true;
    }

    @Override
    public boolean shouldShowScreen(@NonNull Rule rule) {
        return true;
    }

    @Override
    public void onPurchaseCompleted(@NonNull Rule rule, @NonNull ApphudPurchaseResult apphudPurchaseResult) {}
}
