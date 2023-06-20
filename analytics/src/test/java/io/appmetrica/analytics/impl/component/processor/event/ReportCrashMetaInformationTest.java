package io.appmetrica.analytics.impl.component.processor.event;

import io.appmetrica.analytics.IReporter;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.AbstractMap;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class ReportCrashMetaInformationTest extends CommonTest {

    @Mock
    private IReporter reporter;
    @Mock
    private ComponentUnit componentUnit;
    @Mock
    private CounterReport counterReport;

    private ReportCrashMetaInformation handler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new ReportCrashMetaInformation(componentUnit, reporter);
        doReturn(InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF.getTypeId()).when(counterReport).getType();
    }

    @Test
    public void testReturnFalse() {
        assertThat(handler.process(counterReport)).isFalse();
    }

    @Test
    public void testReporting() {
        handler.process(counterReport);
        ArgumentCaptor<HashMap> mapCaptor = ArgumentCaptor.forClass(HashMap.class);
        verify(reporter).reportEvent(eq("crash_saved"), mapCaptor.capture());
        HashMap map = mapCaptor.getValue();
        assertThat(map).containsOnly(new AbstractMap.SimpleEntry("type", "jvm"), new AbstractMap.SimpleEntry("delivery_method", "binder"));
    }

}
