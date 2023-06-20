package io.appmetrica.analytics.billingv3.internal;

import android.content.Context;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender;
import io.appmetrica.analytics.billinginterface.internal.update.UpdatePolicy;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class BillingLibraryMonitorTest {

    @Mock
    private Context context;
    @Mock
    private Executor workerExecutor;
    @Mock
    private Executor uiExecutor;
    @Mock
    private BillingInfoManager billingInfoManager;
    @Mock
    private UpdatePolicy updatePolicy;
    @Mock
    private BillingInfoSender billingInfoSender;

    private BillingLibraryMonitor billingLibraryMonitor;
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

        billingLibraryMonitor = new BillingLibraryMonitor(context, workerExecutor, uiExecutor, billingInfoManager, updatePolicy, billingInfoSender);
    }

    @Test
    public void testOnSessionResumedIfNoConfig() throws Throwable {
        billingLibraryMonitor.onSessionResumed();
        verify(uiExecutor, never()).execute(any(Runnable.class));
    }

    @Test
    public void testOnSessionResumedIfNoConfigAfterNullConfigAndDisabledFeature() throws Throwable {
        billingLibraryMonitor.onBillingConfigChanged(billingConfig);
        billingLibraryMonitor.onBillingConfigChanged(null);
        billingLibraryMonitor.onSessionResumed();
        verify(uiExecutor, never()).execute(any(Runnable.class));
    }

    @Test
    public void testOnSessionResumedIfHasConfig() throws Throwable {
        billingLibraryMonitor.onBillingConfigChanged(billingConfig);
        billingLibraryMonitor.onSessionResumed();
        verify(uiExecutor).execute(any(Runnable.class));
    }

    @Test
    public void testOnSessionResumedIfNullConfig() throws Throwable {
        billingLibraryMonitor.onBillingConfigChanged(null);
        billingLibraryMonitor.onSessionResumed();
        verify(uiExecutor, never()).execute(any(Runnable.class));
    }

    @Test
    public void testOnSessionResumedSequenceOfCalls() throws Throwable {
        billingLibraryMonitor.onSessionResumed();
        verify(uiExecutor, never()).execute(any(Runnable.class));

        billingLibraryMonitor.onBillingConfigChanged(billingConfig);
        billingLibraryMonitor.onSessionResumed();
        verify(uiExecutor).execute(any(Runnable.class));
        clearInvocations(uiExecutor);

        billingLibraryMonitor.onBillingConfigChanged(null);
        billingLibraryMonitor.onSessionResumed();
        verify(uiExecutor, never()).execute(any(Runnable.class));
    }

    @Test
    public void testGetBillingInfoManager() {
        assertThat(billingLibraryMonitor.getBillingInfoManager()).isEqualTo(billingInfoManager);
    }

    @Test
    public void testGetUpdatePolicy() {
        assertThat(billingLibraryMonitor.getUpdatePolicy()).isEqualTo(updatePolicy);
    }

    @Test
    public void testGetBillingInfoSender() {
        assertThat(billingLibraryMonitor.getBillingInfoSender()).isEqualTo(billingInfoSender);
    }

    @Test
    public void testGetUiExecutor() {
        assertThat(billingLibraryMonitor.getUiExecutor()).isEqualTo(uiExecutor);
    }

    @Test
    public void testGetWorkerExecutor() {
        assertThat(billingLibraryMonitor.getWorkerExecutor()).isEqualTo(workerExecutor);
    }
}
