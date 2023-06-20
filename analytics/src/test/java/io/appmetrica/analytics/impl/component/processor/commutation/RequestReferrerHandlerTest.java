package io.appmetrica.analytics.impl.component.processor.commutation;

import android.os.Bundle;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.clients.CommutationClientUnit;
import io.appmetrica.analytics.impl.referrer.common.ReferrerResultReceiver;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class RequestReferrerHandlerTest extends CommonTest {

    @Mock
    private CommutationDispatcherComponent component;
    @Mock
    private CommutationClientUnit clientUnit;
    @Mock
    private CounterReport counterReport;
    private RequestReferrerHandler requestReferrerHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        requestReferrerHandler = new RequestReferrerHandler(component);
    }

    @Test
    public void nullBundle() {
        when(counterReport.getPayload()).thenReturn(null);
        requestReferrerHandler.process(counterReport, clientUnit);
        verify(component).requestReferrer(null);
    }

    @Test
    public void emptyBundle() {
        when(counterReport.getPayload()).thenReturn(new Bundle());
        requestReferrerHandler.process(counterReport, clientUnit);
        verify(component).requestReferrer(null);
    }

    @Test
    public void filledBundle() {
        ReferrerResultReceiver receiver = mock(ReferrerResultReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(ReferrerResultReceiver.BUNDLE_KEY, receiver);
        when(counterReport.getPayload()).thenReturn(bundle);
        requestReferrerHandler.process(counterReport, clientUnit);
        verify(component).requestReferrer(receiver);
    }

}
