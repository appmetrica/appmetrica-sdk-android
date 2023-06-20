package io.appmetrica.analytics.billingv3.impl.library;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider;
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable;
import java.util.concurrent.Executor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BillingClientStateListenerImplTest {

    @Mock
    private Executor workerExecutor;
    @Mock
    private Executor uiExecutor;
    @Mock
    private BillingClient billingClient;
    @Mock
    private UtilsProvider utilsProvider;
    @Mock
    private BillingLibraryConnectionHolder billingLibraryConnectionHolder;

    private final BillingConfig billingConfig = new BillingConfig(41, 42);
    private BillingClientStateListenerImpl billingClientStateListener;

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

        billingClientStateListener = new BillingClientStateListenerImpl(billingConfig, workerExecutor, uiExecutor,
                billingClient, utilsProvider, billingLibraryConnectionHolder);
    }

    @Test
    public void testOnBillingSetupFinishedIfError() throws Throwable {
        billingClientStateListener.onBillingSetupFinished(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                        .build()
        );
        verify(workerExecutor).execute(any(Runnable.class));
        verify(uiExecutor, never()).execute(any(Runnable.class));
        verifyZeroInteractions(billingClient);
        verifyZeroInteractions(billingLibraryConnectionHolder);
    }

    @Test
    public void testOnBillingSetupFinishedIfOk() throws Throwable {
        when(billingClient.isReady()).thenReturn(true);

        billingClientStateListener.onBillingSetupFinished(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.OK)
                        .build()
        );
        verify(billingLibraryConnectionHolder, times(2)).addListener(any());
        verify(billingClient, times(1)).queryPurchaseHistoryAsync(eq(BillingClient.SkuType.INAPP), any(PurchaseHistoryResponseListenerImpl.class));
        verify(billingClient, times(1)).queryPurchaseHistoryAsync(eq(BillingClient.SkuType.SUBS), any(PurchaseHistoryResponseListenerImpl.class));
        verify(billingLibraryConnectionHolder, never()).removeListener(any());
    }

    @Test
    public void testOnSessionResumedIfBillingClientIsNotReady() throws Throwable {
        when(billingClient.isReady()).thenReturn(false);

        billingClientStateListener.onBillingSetupFinished(
                BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.OK)
                        .build()
        );
        verify(billingLibraryConnectionHolder, times(2)).addListener(any());
        verify(billingLibraryConnectionHolder, times(2)).removeListener(any());
        verify(billingClient, never()).queryPurchaseHistoryAsync(any(String.class), any(PurchaseHistoryResponseListener.class));
    }
}
