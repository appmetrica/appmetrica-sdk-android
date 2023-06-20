package io.appmetrica.analytics.billingv3.impl.library;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import io.appmetrica.analytics.billinginterface.internal.Period;
import io.appmetrica.analytics.billinginterface.internal.ProductInfo;
import io.appmetrica.analytics.billinginterface.internal.ProductType;
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender;
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SkuDetailsResponseListenerImplTest {

    @Mock
    private Executor executor;
    @Mock
    private BillingClient billingClient;
    @Mock
    private UtilsProvider utilsProvider;
    @Mock
    private BillingInfoSender billingInfoSender;
    @Mock
    private Map<String, BillingInfo> billingInfoMap;
    @Mock
    private Callable<Void> billingInfoSentListener;
    @Mock
    private BillingLibraryConnectionHolder billingLibraryConnectionHolder;

    private SkuDetailsResponseListenerImpl skuDetailsResponseListener;

    @Before
    public void setUp() throws JSONException {
        MockitoAnnotations.openMocks(this);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            }
        }).when(executor).execute(any(SafeRunnable.class));
        when(utilsProvider.getBillingInfoSender()).thenReturn(billingInfoSender);
        when(billingInfoMap.get("sku1")).thenReturn(new BillingInfo(ProductType.INAPP, "sku1", "token1", 10, 1));
        when(billingInfoMap.get("sku2")).thenReturn(new BillingInfo(ProductType.SUBS, "sku2", "token2", 11, 1));
        when(billingClient.queryPurchases(anyString())).thenReturn(getPurchasesResult());

        skuDetailsResponseListener = new SkuDetailsResponseListenerImpl(BillingClient.SkuType.INAPP, executor, billingClient, utilsProvider, billingInfoSentListener, billingInfoMap, billingLibraryConnectionHolder);
    }

    @Test
    public void testOnSkuDetailsResponseIfError() {
        skuDetailsResponseListener.onSkuDetailsResponse(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                        .build(),
                null
        );
        verify(executor).execute(any(Runnable.class));
        verify(billingLibraryConnectionHolder).removeListener(skuDetailsResponseListener);
        verifyZeroInteractions(billingInfoSender);
        verifyZeroInteractions(billingInfoSentListener);
    }

    @Test
    public void testOnSkuDetailsResponseIfOk() throws Exception {
        skuDetailsResponseListener.onSkuDetailsResponse(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.OK)
                        .build(),
                getSkuDetailsList()
        );
        verify(executor).execute(any(Runnable.class));
        verify(billingLibraryConnectionHolder).removeListener(skuDetailsResponseListener);

        ArgumentCaptor<List<ProductInfo>> argument = ArgumentCaptor.forClass(List.class);
        verify(billingInfoSender).sendInfo(argument.capture());
        assertThat(argument.getValue()).isEqualTo(getProductInfoList());
        verify(billingInfoSentListener).call();
    }

    @Test
    public void testOnSkuDetailsResponseIfOkAndNullList() {
        skuDetailsResponseListener.onSkuDetailsResponse(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.OK)
                        .build(),
                null
        );
        verify(executor).execute(any(Runnable.class));
        verify(billingLibraryConnectionHolder).removeListener(skuDetailsResponseListener);
        verifyZeroInteractions(billingInfoSender);
        verifyZeroInteractions(billingInfoSentListener);
    }

    @Test
    public void testOnSkuDetailsResponseIfOkAndEmptyList() {
        skuDetailsResponseListener.onSkuDetailsResponse(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.OK)
                        .build(),
                new ArrayList<SkuDetails>()
        );
        verify(executor).execute(any(Runnable.class));
        verify(billingLibraryConnectionHolder).removeListener(skuDetailsResponseListener);
        verifyZeroInteractions(billingInfoSender);
        verifyZeroInteractions(billingInfoSender);
    }

    @Test
    public void testOnSkuDetailsResponseIfOkAndPurchasesIsNull() throws Exception {
        when(billingClient.queryPurchases(anyString())).thenReturn(getPurchasesResultWithNullList());

        skuDetailsResponseListener.onSkuDetailsResponse(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.OK)
                        .build(),
                getSkuDetailsList()
        );
        verify(executor).execute(any(Runnable.class));
        verify(billingLibraryConnectionHolder).removeListener(skuDetailsResponseListener);

        ArgumentCaptor<List<ProductInfo>> argument = ArgumentCaptor.forClass(List.class);
        verify(billingInfoSender).sendInfo(argument.capture());
        assertThat(argument.getValue()).isEqualTo(getProductInfoListIfEmptyPurchases());
        verify(billingInfoSentListener).call();
    }

    @Test
    public void testOnSkuDetailsResponseIfOkAndErrorInPurchases() throws Exception {
        when(billingClient.queryPurchases(anyString())).thenReturn(getPurchasesResultWithErrorResult());

        skuDetailsResponseListener.onSkuDetailsResponse(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.OK)
                        .build(),
                getSkuDetailsList()
        );
        verify(executor).execute(any(Runnable.class));
        verify(billingLibraryConnectionHolder).removeListener(skuDetailsResponseListener);

        ArgumentCaptor<List<ProductInfo>> argument = ArgumentCaptor.forClass(List.class);
        verify(billingInfoSender).sendInfo(argument.capture());
        assertThat(argument.getValue()).isEqualTo(getProductInfoListIfEmptyPurchases());
        verify(billingInfoSentListener).call();
    }

    private List<SkuDetails> getSkuDetailsList() throws JSONException {
        return Arrays.asList(
                new SkuDetails("{\"productId\":\"sku1\", \"type\":\"inapp\", \"price_amount_micros\":1, \"price_currency_code\":\"by\", \"introductoryPriceAmountMicros\": 3, \"introductoryPricePeriod\": \"P1D\", \"introductoryPriceCycles\": 1, \"subscriptionPeriod\": \"P1M\"}"),
                new SkuDetails("{\"productId\":\"sku2\", \"type\":\"subs\", \"price_amount_micros\":2, \"price_currency_code\":\"by\", \"introductoryPriceAmountMicros\": 4, \"introductoryPricePeriod\": \"P1D\", \"introductoryPriceCycles\": 1, \"subscriptionPeriod\": \"P1M\"}"),
                new SkuDetails("{\"productId\":\"sku3\", \"type\":\"type\", \"price_amount_micros\":3, \"price_currency_code\":\"by\", \"introductoryPriceAmountMicros\": 5, \"introductoryPricePeriod\": \"P1D\", \"introductoryPriceCycles\": 1, \"subscriptionPeriod\": \"P1M\"}")
        );
    }

    private List<ProductInfo> getProductInfoList() {
        return Arrays.asList(
                new ProductInfo(ProductType.INAPP, "sku1", 1, 1, "by", 3, new Period(1, Period.TimeUnit.DAY), 1, new Period(1, Period.TimeUnit.MONTH), "signature", "token1", 10, true, "{\"productId\":\"sku1\", \"autoRenewing\":\"true\"}"),
                new ProductInfo(ProductType.SUBS, "sku2", 1, 2, "by", 4, new Period(1, Period.TimeUnit.DAY), 1, new Period(1, Period.TimeUnit.MONTH), "", "token2", 11, false, "{}")
        );
    }

    private List<ProductInfo> getProductInfoListIfEmptyPurchases() {
        return Arrays.asList(
                new ProductInfo(ProductType.INAPP, "sku1", 1, 1, "by", 3, new Period(1, Period.TimeUnit.DAY), 1, new Period(1, Period.TimeUnit.MONTH), "", "token1", 10, false, "{}"),
                new ProductInfo(ProductType.SUBS, "sku2", 1, 2, "by", 4, new Period(1, Period.TimeUnit.DAY), 1, new Period(1, Period.TimeUnit.MONTH), "", "token2", 11, false, "{}")
        );
    }

    private Purchase.PurchasesResult getPurchasesResult() throws JSONException {
        return new Purchase.PurchasesResult(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.OK)
                        .build(),
                Collections.singletonList(
                        new Purchase("{\"productId\":\"sku1\", \"autoRenewing\":\"true\"}", "signature")
                ));
    }

    private Purchase.PurchasesResult getPurchasesResultWithNullList() {
        return new Purchase.PurchasesResult(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.OK)
                        .build(),
                null
        );
    }

    private Purchase.PurchasesResult getPurchasesResultWithErrorResult() {
        return new Purchase.PurchasesResult(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                        .build(),
                null
        );
    }
}
