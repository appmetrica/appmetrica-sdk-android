package io.appmetrica.analytics.impl.component.processor;

import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.component.IComponent;
import io.appmetrica.gradle.testutils.CommonTest;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BaseReportProcessorTest extends CommonTest {

    protected BaseReportProcessor mProcessor;
    protected ProcessingStrategyFactory mFactory;
    protected IComponent mComponent;
    protected Object mHandler1;
    protected Object mHandler2;

    @Before
    public void setUp() {
        mFactory = mock(ProcessingStrategyFactory.class);
        mComponent = mock(IComponent.class);
        mProcessor = new BaseReportProcessor(mFactory, mComponent);
        EventProcessingStrategy strategy = mock(EventProcessingStrategy.class);
        mHandler1 = new Object();
        mHandler2 = new Object();
        doReturn(Arrays.asList(mHandler1, mHandler2)).when(strategy).getEventHandlers();
        doReturn(strategy).when(mFactory).getProcessingStrategy(anyInt());
    }

    @Test
    public void testGetComponent() {
        assertThat(mProcessor.getComponent()).isEqualTo(mComponent);
    }

    @Test
    public void testProcess() {
        BaseReportProcessor.ProcessItem item = mock(BaseReportProcessor.ProcessItem.class);
        doReturn(false).when(item).process(any(), any(CoreServiceEvent.class));
        CoreServiceEvent serviceEvent = new CoreServiceEvent();
        mProcessor.process(serviceEvent, item);
        verify(item, times(1)).process(mHandler1, serviceEvent);
        verify(item, times(1)).process(mHandler2, serviceEvent);
    }

    @Test
    public void testBreakChain() {
        BaseReportProcessor.ProcessItem item = mock(BaseReportProcessor.ProcessItem.class);
        CoreServiceEvent serviceEvent = new CoreServiceEvent();
        doReturn(true).when(item).process(mHandler1, serviceEvent);
        doReturn(false).when(item).process(mHandler2, serviceEvent);
        mProcessor.process(serviceEvent, item);
        verify(item, times(1)).process(mHandler1, serviceEvent);
        verify(item, never()).process(mHandler2, serviceEvent);
    }
}
