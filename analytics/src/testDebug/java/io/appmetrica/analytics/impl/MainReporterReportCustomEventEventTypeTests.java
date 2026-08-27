package io.appmetrica.analytics.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class MainReporterReportCustomEventEventTypeTests extends
    ReporterReportCustomEventEventTypeBaseTests {

    @Mock
    private MainReporterComponents mainReporterComponents;

    public MainReporterReportCustomEventEventTypeTests(int eventType, int wantedNumberOfInvocations) {
        super(eventType, wantedNumberOfInvocations);
    }

    @Override
    public BaseReporter getReporter() {
        when(mainReporterComponents.getContext()).thenReturn(mContext);
        when(mainReporterComponents.getReportsHandler()).thenReturn(mReportsHandler);
        when(mainReporterComponents.getReporterEnvironment()).thenReturn(mReporterEnvironment);
        when(mainReporterComponents.getProcessDetector()).thenReturn(processNameProvider);
        when(mainReporterComponents.getAppStatusMonitor()).thenReturn(mAppStatusMonitor);
        when(mainReporterComponents.getNativeCrashClient()).thenReturn(nativeCrashClient);
        return new MainReporter(
            mainReporterComponents
        );
    }
}
