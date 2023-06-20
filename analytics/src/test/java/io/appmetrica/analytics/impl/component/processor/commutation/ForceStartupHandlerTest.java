package io.appmetrica.analytics.impl.component.processor.commutation;

import android.os.Bundle;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.IdentifiersData;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.clients.CommutationClientUnit;
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
public class ForceStartupHandlerTest  extends CommonTest {

    @Mock
    private CommutationDispatcherComponent mCommutationDispatcherComponent;
    @Mock
    private CommutationClientUnit mCommutationClientUnit;
    @Mock
    private CounterReport mCounterReport;
    private ForceStartupHandler mForceStartupHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mForceStartupHandler = new ForceStartupHandler(mCommutationDispatcherComponent);
    }

    @Test
    public void nullBundle() {
        when(mCounterReport.getPayload()).thenReturn(null);
        mForceStartupHandler.process(mCounterReport, mCommutationClientUnit);
        verify(mCommutationDispatcherComponent).provokeStartupOrGetCurrentState(null);
    }

    @Test
    public void emptyBundle() {
        when(mCounterReport.getPayload()).thenReturn(new Bundle());
        mForceStartupHandler.process(mCounterReport, mCommutationClientUnit);
        verify(mCommutationDispatcherComponent).provokeStartupOrGetCurrentState(null);
    }

    @Test
    public void filledBundle() {
        IdentifiersData identifiersData = mock(IdentifiersData.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(IdentifiersData.BUNDLE_KEY, identifiersData);
        when(mCounterReport.getPayload()).thenReturn(bundle);
        mForceStartupHandler.process(mCounterReport, mCommutationClientUnit);
        verify(mCommutationDispatcherComponent).provokeStartupOrGetCurrentState(identifiersData);
    }
}
