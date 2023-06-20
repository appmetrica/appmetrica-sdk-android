package io.appmetrica.analytics.billingv3.impl.library;

import android.os.Handler;
import com.android.billingclient.api.BillingClient;
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class BillingLibraryConnectionHolderTest {

    private BillingLibraryConnectionHolder holder;
    @Mock
    private BillingClient billingClient;
    @Mock
    private Handler mainHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            }
        }).when(mainHandler).post(any(SafeRunnable.class));

        holder = new BillingLibraryConnectionHolder(billingClient, mainHandler);
    }

    @Test
    public void testListeners() {
        Object listener1 = new Object();
        Object listener2 = new Object();

        holder.addListener(listener1);
        holder.addListener(listener2);
        verify(billingClient, never()).endConnection();

        holder.removeListener(listener1);
        verify(billingClient, never()).endConnection();

        holder.removeListener(listener2);
        verify(billingClient).endConnection();
    }
}
