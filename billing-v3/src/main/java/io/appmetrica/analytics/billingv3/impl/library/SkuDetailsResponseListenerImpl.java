package io.appmetrica.analytics.billingv3.impl.library;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import io.appmetrica.analytics.billinginterface.internal.Period;
import io.appmetrica.analytics.billinginterface.internal.ProductInfo;
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider;
import io.appmetrica.analytics.billingv3.impl.BillingUtils;
import io.appmetrica.analytics.billingv3.impl.ProductTypeParser;
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

class SkuDetailsResponseListenerImpl implements SkuDetailsResponseListener {

    private static final String TAG = "[SkuDetailsResponseListenerImpl]";

    @NonNull
    private final String type;
    @NonNull
    private final Executor workerExecutor;
    @NonNull
    private final BillingClient billingClient;
    @NonNull
    private final UtilsProvider utilsProvider;
    @NonNull
    private final Callable<Void> billingInfoSentListener;
    @NonNull
    private final Map<String, BillingInfo> billingInfoMap;
    @NonNull
    private final BillingLibraryConnectionHolder billingLibraryConnectionHolder;

    SkuDetailsResponseListenerImpl(@NonNull final String type,
                                   @NonNull final Executor workerExecutor,
                                   @NonNull final BillingClient billingClient,
                                   @NonNull final UtilsProvider utilsProvider,
                                   @NonNull final Callable<Void> billingInfoSentListener,
                                   @NonNull final Map<String, BillingInfo> billingInfoMap,
                                   @NonNull final BillingLibraryConnectionHolder billingLibraryConnectionHolder) {
        this.type = type;
        this.workerExecutor = workerExecutor;
        this.billingClient = billingClient;
        this.utilsProvider = utilsProvider;
        this.billingInfoSentListener = billingInfoSentListener;
        this.billingInfoMap = billingInfoMap;
        this.billingLibraryConnectionHolder = billingLibraryConnectionHolder;
    }

    @UiThread
    @Override
    public void onSkuDetailsResponse(@NonNull final BillingResult billingResult,
                                     @Nullable final List<SkuDetails> list) {
        workerExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() throws Throwable {
                processResponse(billingResult, list);
                billingLibraryConnectionHolder.removeListener(SkuDetailsResponseListenerImpl.this);
            }
        });
    }

    @WorkerThread
    private void processResponse(@NonNull final BillingResult billingResult,
                                 @Nullable final List<SkuDetails> list) throws Throwable {
        YLogger.info(TAG, "onSkuDetailsResponse type=%s, result=%s, list=%s",
                type, BillingUtils.toString(billingResult), list
        );
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && list != null && !list.isEmpty()) {
            final Map<String, Purchase> purchases = getPurchases();
            final List<ProductInfo> productInfos = new ArrayList<>();
            for (final SkuDetails record: list) {
                final BillingInfo billingInfo = billingInfoMap.get(record.getSku());
                final Purchase purchase = purchases.get(record.getSku());
                if (billingInfo != null) {
                    final ProductInfo info = getProductInfo(record, billingInfo, purchase);
                    YLogger.debug(TAG, "Billing info from sku details %s", info);
                    productInfos.add(info);
                }
            }
            utilsProvider.getBillingInfoSender().sendInfo(productInfos);
            billingInfoSentListener.call();
        }
    }

    @NonNull
    private Map<String, Purchase> getPurchases() {
        final Map<String, Purchase> result = new HashMap<>();
        final Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(type);
        final List<Purchase> purchases = purchasesResult.getPurchasesList();
        if (purchasesResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (final Purchase entry : purchases) {
                result.put(entry.getSku(), entry);
            }
        }
        return result;
    }

    @NonNull
    private ProductInfo getProductInfo(@NonNull final SkuDetails skuDetails,
                                       @NonNull final BillingInfo billingInfo,
                                       @Nullable final Purchase purchase) {
        return new ProductInfo(
                ProductTypeParser.parse(skuDetails.getType()),
                skuDetails.getSku(),
                1,
                skuDetails.getPriceAmountMicros(),
                skuDetails.getPriceCurrencyCode(),
                getIntroductoryPriceAmountMicros(skuDetails),
                getIntroductoryPricePeriod(skuDetails),
                getIntroductoryPriceCycles(skuDetails),
                Period.parse(skuDetails.getSubscriptionPeriod()),
                purchase != null ? purchase.getSignature() : "",
                billingInfo.purchaseToken,
                billingInfo.purchaseTime,
                purchase != null ? purchase.isAutoRenewing() : false,
                purchase != null ? purchase.getOriginalJson() : "{}"
        );
    }

    private int getIntroductoryPriceCycles(@NonNull final SkuDetails skuDetails) {
        if (skuDetails.getFreeTrialPeriod().isEmpty()) {
            // in version com.android.billingclient:billing:3.+ method getIntroductoryPriceCycles returns int
            // in version com.android.billingclient:billing:2.+ method getIntroductoryPriceCycles returns String
            try {
                return skuDetails.getIntroductoryPriceCycles();
            } catch (Throwable e1) {
                YLogger.error(TAG, e1);
                try {
                    final String method = "getIntroductoryPriceCycles";
                    final String result = (String) skuDetails.getClass().getMethod(method).invoke(skuDetails);
                    if (result != null) {
                        return Integer.parseInt(result);
                    } else {
                        return 0;
                    }
                } catch (Throwable e2) {
                    YLogger.error(TAG, e2);
                }
            }
            return 0;
        } else {
            return 1;
        }
    }

    private long getIntroductoryPriceAmountMicros(@NonNull final SkuDetails skuDetails) {
        if (skuDetails.getFreeTrialPeriod().isEmpty()) {
            return skuDetails.getIntroductoryPriceAmountMicros();
        } else {
            return 0;
        }
    }

    private Period getIntroductoryPricePeriod(@NonNull final SkuDetails skuDetails) {
        if (skuDetails.getFreeTrialPeriod().isEmpty()) {
            return Period.parse(skuDetails.getIntroductoryPricePeriod());
        } else {
            return Period.parse(skuDetails.getFreeTrialPeriod());
        }
    }
}
