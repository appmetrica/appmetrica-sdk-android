package io.appmetrica.analytics.impl.component.processor;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.processor.event.EventHandler;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
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
public class ReportingReportProcessorTest extends CommonTest {

    private ReportingReportProcessor mProcessor;
    private ProcessingStrategyFactory<EventHandler> mFactory;
    protected ComponentUnit mComponent;
    private EventHandler mHandler;

    @Before
    public void setUp() {
        mFactory = mock(ProcessingStrategyFactory.class);
        mComponent = mock(ComponentUnit.class);
        mProcessor = new ReportingReportProcessor(mFactory, mComponent);
        EventProcessingStrategy strategy = mock(EventProcessingStrategy.class);
        mHandler = mock(EventHandler.class);
        doReturn(Arrays.asList(mHandler)).when(strategy).getEventHandlers();
        doReturn(strategy).when(mFactory).getProcessingStrategy(anyInt());
    }

    @Test
    public void testHandlerCalledWithClientUnit() {
        CounterReport report = new CounterReport();
        mProcessor.process(report);
        verify(mHandler, times(1)).process(report);
    }

}
