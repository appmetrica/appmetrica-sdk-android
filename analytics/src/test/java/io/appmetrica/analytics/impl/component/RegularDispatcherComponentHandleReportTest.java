package io.appmetrica.analytics.impl.component;

import android.content.Context;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.clients.ClientUnit;
import io.appmetrica.analytics.impl.component.clients.ComponentUnitFactory;
import io.appmetrica.analytics.impl.request.StartupArgumentsTest;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.impl.startup.StartupCenter;
import io.appmetrica.analytics.impl.startup.StartupUnit;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class RegularDispatcherComponentHandleReportTest extends CommonTest {

    @Mock
    private ComponentId mComponentId;
    private StartupRequestConfig.Arguments startupArguments = StartupArgumentsTest.empty();
    private CommonArguments.ReporterArguments reportArguments = CommonArgumentsTestUtils.emptyReporterArguments();
    private CommonArguments mClientConfiguration;
    @Mock
    private ComponentUnitFactory<ComponentUnit> mComponentUnitFactory;
    @Mock
    private ComponentUnit mReportingComponent;
    @Mock
    private ComponentLifecycleManager<ClientUnit> mLifecycleManager;
    @Mock
    private ReporterArgumentsHolder mReporterArgumentsHolder;

    private RegularDispatcherComponent<ComponentUnit> mRegularDispatcherComponent;
    private CounterReport mCounterReport;
    private Context mContext;

    private int mEventType;

    public RegularDispatcherComponentHandleReportTest(int eventType) {
        mEventType = eventType;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "[{index}]Event with type = {0} handle as diagnostic ? {1}")
    public static Collection<Object[]> data() {
        Collection<Object[]> data = new ArrayList<Object[]>();
        for (InternalEvents eventType : InternalEvents.values()) {
            data.add(new Object[]{eventType.getTypeId()});
        }

        return data;
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(mReporterArgumentsHolder.getArguments()).thenReturn(reportArguments);
        mContext = RuntimeEnvironment.getApplication();
        when(mComponentUnitFactory.createComponentUnit(same(mContext), same(mComponentId), any(CommonArguments.ReporterArguments.class), any(StartupUnit.class)))
                .thenReturn(mReportingComponent);
        mClientConfiguration = new CommonArguments(startupArguments, reportArguments, null);
        StartupCenter startupCenter = mock(StartupCenter.class);
        doReturn(mock(StartupUnit.class)).when(startupCenter).getOrCreateStartupUnit(any(Context.class), any(ComponentId.class), any(StartupRequestConfig.Arguments.class));
        mRegularDispatcherComponent = new RegularDispatcherComponent<ComponentUnit>(
                mContext,
                mComponentId,
                mClientConfiguration,
                mReporterArgumentsHolder,
                mComponentUnitFactory,
                mLifecycleManager,
                startupCenter
        );

        mCounterReport = new CounterReport();
        mCounterReport.setType(mEventType);
    }

    @Test
    public void testHandleReport() {
        mRegularDispatcherComponent.handleReport(mCounterReport, CommonArgumentsTestUtils.createMockedArguments());
        verify(mReportingComponent, times(1)).handleReport(mCounterReport);
    }
}
