package io.appmetrica.analytics.billingv3.impl.library;

import androidx.annotation.NonNull;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import io.appmetrica.analytics.billinginterface.internal.ProductType;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager;
import io.appmetrica.analytics.billinginterface.internal.update.UpdatePolicy;
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PurchaseHistoryResponseListenerImplTest {

    @Mock
    private Executor workerExecutor;
    @Mock
    private Executor uiExecutor;
    @Mock
    private BillingClient billingClient;
    @Mock
    private UtilsProvider utilsProvider;
    @Mock
    private UpdatePolicy updatePolicy;
    @Mock
    private BillingInfoManager billingInfoManager;
    @Mock
    private BillingLibraryConnectionHolder billingLibraryConnectionHolder;
    @Mock
    private SystemTimeProvider systemTimeProvider;

    private PurchaseHistoryResponseListenerImpl purchaseHistoryResponseListener;
    private final long now = 4142;
    private final BillingConfig billingConfig = new BillingConfig(41, 42);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            }
        }).when(workerExecutor).execute(any(SafeRunnable.class));
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            }
        }).when(uiExecutor).execute(any(SafeRunnable.class));
        when(utilsProvider.getUpdatePolicy()).thenReturn(updatePolicy);
        when(utilsProvider.getBillingInfoManager()).thenReturn(billingInfoManager);
        when(systemTimeProvider.currentTimeMillis()).thenReturn(now);

        purchaseHistoryResponseListener = new PurchaseHistoryResponseListenerImpl(billingConfig, workerExecutor, uiExecutor, billingClient, utilsProvider, BillingClient.SkuType.SUBS, billingLibraryConnectionHolder, systemTimeProvider);
    }

    @Test
    public void testOnPurchaseHistoryResponseIfError() {
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                        .build(),
                null
        );
        verifyNoMoreInteractions(billingClient);
        verifyNoMoreInteractions(billingInfoManager);
        verify(billingLibraryConnectionHolder).removeListener(purchaseHistoryResponseListener);
    }

    @Test
    public void testOnPurchaseHistoryResponseIfNullList() {
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.OK)
                        .build(),
                null
        );
        verifyNoMoreInteractions(billingClient);
        verifyNoMoreInteractions(billingInfoManager);
        verify(billingLibraryConnectionHolder).removeListener(purchaseHistoryResponseListener);
    }

    @Test
    public void testOnPurchaseHistoryResponseIfOk() throws JSONException {
        when(billingClient.isReady()).thenReturn(true);
        when(updatePolicy.getBillingInfoToUpdate(
                any(BillingConfig.class),
                ArgumentMatchers.<String, BillingInfo>anyMap(),
                any(BillingInfoManager.class)
        )).thenReturn(getBillingInfoToUpdate(ProductType.SUBS));

        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.OK)
                        .build(),
                getPurchaseHistory()
        );
        ArgumentCaptor<Map<String, BillingInfo>> argument = ArgumentCaptor.forClass(Map.class);
        verify(updatePolicy).getBillingInfoToUpdate(eq(billingConfig), argument.capture(), eq(billingInfoManager));
        assertThat(argument.getValue()).usingRecursiveComparison().isEqualTo(getBillingInfoToUpdate(ProductType.SUBS));

        verifyNoMoreInteractions(billingInfoManager);
        verify(billingLibraryConnectionHolder).addListener(any(SkuDetailsResponseListenerImpl.class));
        verify(billingLibraryConnectionHolder, never()).removeListener(any(SkuDetailsResponseListenerImpl.class));
        verify(billingLibraryConnectionHolder).removeListener(purchaseHistoryResponseListener);
        verify(billingClient).querySkuDetailsAsync(any(SkuDetailsParams.class), any(SkuDetailsResponseListener.class));
    }

    @Test
    public void testUpdateStorageForNonEmptyNewBillingInfo() {
        when(billingInfoManager.isFirstInappCheckOccurred()).thenReturn(true);
        final Map<String, BillingInfo> history = new HashMap<>();
        history.put("sku1", new BillingInfo(ProductType.INAPP, "sku1", "token1", 10, 43));
        history.put("sku2", new BillingInfo(ProductType.INAPP, "sku2", "token2", 10, 42));

        final Map<String, BillingInfo> newBillingInfo = new HashMap<>();
        newBillingInfo.put("sku1", new BillingInfo(ProductType.INAPP, "sku1", "token1", 10, 0));

        purchaseHistoryResponseListener.updateStorage(history, newBillingInfo);

        final Map<String, BillingInfo> changedHistory = new HashMap<>();
        changedHistory.put("sku1", new BillingInfo(ProductType.INAPP, "sku1", "token1", 10, now));
        changedHistory.put("sku2", new BillingInfo(ProductType.INAPP, "sku2", "token2", 10, 42));

        ArgumentCaptor<Map<String, BillingInfo>> captor = ArgumentCaptor.forClass(Map.class);
        verify(billingInfoManager).update(captor.capture());
        assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(changedHistory);
    }

    @Test
    public void testOnPurchaseHistoryResponseIfOkAndUpdatePolicyReturnsEmptyMapAndFirstInappCheckOccurred() throws JSONException {
        when(billingClient.isReady()).thenReturn(true);
        when(updatePolicy.getBillingInfoToUpdate(
                any(BillingConfig.class),
                ArgumentMatchers.<String, BillingInfo>anyMap(),
                any(BillingInfoManager.class)
        )).thenReturn(new HashMap<String, BillingInfo>());
        when(billingInfoManager.isFirstInappCheckOccurred()).thenReturn(true);
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.OK)
                        .build(),
                getPurchaseHistory()
        );
        ArgumentCaptor<Map<String, BillingInfo>> captor = ArgumentCaptor.forClass(Map.class);
        verify(billingInfoManager).update(captor.capture());
        assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(getBillingInfoToUpdate(ProductType.SUBS));
        verify(billingInfoManager, never()).markFirstInappCheckOccurred();
    }

    @Test
    public void testOnPurchaseHistoryResponseIfOkAndUpdatePolicyReturnsEmptyMapAndFirstInappCheckNotOccurredAndTypeInapp() throws JSONException {
        purchaseHistoryResponseListener = new PurchaseHistoryResponseListenerImpl(billingConfig, workerExecutor, uiExecutor, billingClient, utilsProvider, BillingClient.SkuType.INAPP, billingLibraryConnectionHolder, systemTimeProvider);
        when(billingClient.isReady()).thenReturn(true);
        when(updatePolicy.getBillingInfoToUpdate(
                any(BillingConfig.class),
                ArgumentMatchers.<String, BillingInfo>anyMap(),
                any(BillingInfoManager.class)
        )).thenReturn(new HashMap<String, BillingInfo>());
        when(billingInfoManager.isFirstInappCheckOccurred()).thenReturn(false);
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.OK)
                        .build(),
                getPurchaseHistory()
        );
        ArgumentCaptor<Map<String, BillingInfo>> captor = ArgumentCaptor.forClass(Map.class);
        verify(billingInfoManager).update(captor.capture());
        assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(getBillingInfoToUpdate(ProductType.INAPP));
        verify(billingInfoManager).markFirstInappCheckOccurred();
    }

    @Test
    public void testOnPurchaseHistoryResponseIfOkAndUpdatePolicyReturnsEmptyMapAndFirstInappCheckNotOccurredAndTypeNotInapp() throws JSONException {
        purchaseHistoryResponseListener = new PurchaseHistoryResponseListenerImpl(billingConfig, workerExecutor, uiExecutor, billingClient, utilsProvider, BillingClient.SkuType.SUBS, billingLibraryConnectionHolder, systemTimeProvider);
        when(billingClient.isReady()).thenReturn(true);
        when(updatePolicy.getBillingInfoToUpdate(
                any(BillingConfig.class),
                ArgumentMatchers.<String, BillingInfo>anyMap(),
                any(BillingInfoManager.class)
        )).thenReturn(new HashMap<String, BillingInfo>());
        when(billingInfoManager.isFirstInappCheckOccurred()).thenReturn(false);
        purchaseHistoryResponseListener.onPurchaseHistoryResponse(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.OK)
                        .build(),
                getPurchaseHistory()
        );
        ArgumentCaptor<Map<String, BillingInfo>> captor = ArgumentCaptor.forClass(Map.class);
        verify(billingInfoManager).update(captor.capture());
        assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(getBillingInfoToUpdate(ProductType.SUBS));
        verify(billingInfoManager, never()).markFirstInappCheckOccurred();
    }

    private List<PurchaseHistoryRecord> getPurchaseHistory() throws JSONException {
        final List<PurchaseHistoryRecord> purchaseHistoryRecords = new ArrayList<>();
        purchaseHistoryRecords.add(new PurchaseHistoryRecord(
                new JSONObject()
                        .put("productId", "sku1")
                        .put("purchaseToken", "token1")
                        .put("purchaseTime", 10)
                        .toString(),
                "signature"
        ));
        return purchaseHistoryRecords;
    }

    private Map<String, BillingInfo> getBillingInfoToUpdate(@NonNull final ProductType type) {
        final Map<String, BillingInfo> result = new HashMap<>();
        result.put("sku1", new BillingInfo(type, "sku1", "token1", 10, 0));
        return result;
    }
}
