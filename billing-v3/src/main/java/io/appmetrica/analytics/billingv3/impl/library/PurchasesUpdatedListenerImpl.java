package io.appmetrica.analytics.billingv3.impl.library;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import io.appmetrica.analytics.billingv3.impl.BillingUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import java.util.List;

public class PurchasesUpdatedListenerImpl implements PurchasesUpdatedListener {

    private static final String TAG = "[PurchasesUpdatedListenerImpl]";

    @UiThread
    @Override
    public void onPurchasesUpdated(@NonNull final BillingResult billingResult,
                                   @Nullable final List<Purchase> list) {
        YLogger.info(TAG, "onPurchasesUpdated %s", BillingUtils.toString(billingResult));
    }
}
