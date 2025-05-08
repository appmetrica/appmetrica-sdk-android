package io.appmetrica.analytics.impl.component.processor.commutation;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.clients.CommutationClientUnit;
import io.appmetrica.analytics.impl.component.processor.EventProcessingStrategy;
import io.appmetrica.analytics.impl.component.processor.ProcessingStrategyFactory;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class CommutationReportProcessorTest extends CommonTest {

    private CommutationReportProcessor mProcessor;
    private ProcessingStrategyFactory<CommutationHandler> mFactory;
    protected CommutationDispatcherComponent mComponent;
    private CommutationHandler mHandler;

    @Before
    public void setUp() {
        mFactory = mock(ProcessingStrategyFactory.class);
        mComponent = mock(CommutationDispatcherComponent.class);
        mProcessor = new CommutationReportProcessor(mFactory, mComponent);
        EventProcessingStrategy strategy = mock(EventProcessingStrategy.class);
        mHandler = mock(CommutationHandler.class);
        doReturn(Collections.singletonList(mHandler)).when(strategy).getEventHandlers();
        doReturn(strategy).when(mFactory).getProcessingStrategy(anyInt());
    }

    @Test
    public void testHandlerCalledWithClientUnit() {
        CounterReport report = new CounterReport();
        CommutationClientUnit unit = mock(CommutationClientUnit.class);
        mProcessor.process(report, unit);
        verify(mHandler, times(1)).process(report, unit);
    }
}
