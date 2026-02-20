package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.ModuleEvent;
import io.appmetrica.analytics.impl.service.AppMetricaServiceDataReporter;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class ReporterReportCustomEventEventTypeBaseTests extends BaseReporterData {

    private static final Collection<Integer> RESERVED_EVENT_TYPE_EXCEPTIONS = Arrays.asList(1, 13);

    private static HashSet<Integer> getReservedEventTypes() {
        HashSet<Integer> types = new HashSet<Integer>();
        for (int i = 1; i < 100; i++) {
            if (RESERVED_EVENT_TYPE_EXCEPTIONS.contains(i)) {
                types.add(i);
            }
        }

        return types;
    }

    @Parameterized.Parameters(name = "Report custom event with type={0}")
    public static Collection<Object[]> data() {
        ArrayList<Object[]> data = new ArrayList<Object[]>();
        HashSet<Integer> reserved = getReservedEventTypes();
        for (int i = -1; i < 102; i++) {
            data.add(new Object[]{i, reserved.contains(i) ? 0 : 1});
        }
        return data;
    }

    final int mEventType;
    final int mWantedNumbersOfInvocation;

    @Rule
    public final MockedStaticRule<EventsManager> sEventsManager = new MockedStaticRule<>(EventsManager.class);
    private final CounterReport mockedEvent = mock(CounterReport.class);
    protected BaseReporter mReporter;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        mReporter = getReporter();
        when(mReporterEnvironment.getReporterConfiguration()).thenReturn(mCounterConfiguration);
        mReporter.setKeepAliveHandler(mKeepAliveHandler);
    }

    public ReporterReportCustomEventEventTypeBaseTests(int eventType, int wantedNumberOfInvocations) {
        mEventType = eventType;
        mWantedNumbersOfInvocation = wantedNumberOfInvocations;
    }

    @Test
    public void testShouldIgnoringMetricaInternalEventTypes() {
        int serviceDataReporterType = AppMetricaServiceDataReporter.TYPE_CORE;

        final ModuleEvent moduleEvent = ModuleEvent.newBuilder(mEventType)
            .withName(randomString())
            .withValue(randomString())
            .withServiceDataReporterType(serviceDataReporterType)
            .withEnvironment(new HashMap<String, Object>())
            .withExtras(new HashMap<String, byte[]>())
            .withAttributes(new HashMap<String, Object>())
            .build();

        when(EventsManager.customEventReportEntry(any(), any(PublicLogger.class))).thenReturn(mockedEvent);
        mReporter.reportEvent(moduleEvent);
        verify(mReportsHandler, times(mWantedNumbersOfInvocation))
            .reportEvent(
                same(mockedEvent),
                any(ReporterEnvironment.class),
                same(serviceDataReporterType),
                any(Map.class)
            );
    }

    public abstract BaseReporter getReporter();
}
