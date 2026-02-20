package io.appmetrica.analytics.impl.component.processor.commutation;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.DataSendingRestrictionControllerImpl;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommutationComponentId;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.clients.CommutationClientUnit;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.testutils.ContextRule;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class UpdatePreActivationConfigHandlerLocationTest {

    @Rule
    public ContextRule contextRule = new ContextRule();

    @Rule
    public final GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Mock
    private CommutationClientUnit mClientUnit;
    @Mock
    private CommutationDispatcherComponent mRegularDispatcherComponent;
    @Mock
    private CounterReport mCounterReport;
    @Mock
    private CounterConfiguration mCounterConfiguration;
    @Mock
    private DataSendingRestrictionControllerImpl mRestrictionController;

    @Nullable
    private final Boolean locationTracking;
    @Nullable
    private final Boolean shouldSetWhat;

    private UpdatePreActivationConfigHandler mHandler;

    @Parameterized.Parameters(name = "locationTracking = {0}, allowedByBridge = {1}")
    public static Collection<Object[]> getData() {
        return Arrays.asList(
            new Object[]{null, null},
            new Object[]{false, false},
            new Object[]{true, true}
        );
    }

    public UpdatePreActivationConfigHandlerLocationTest(@Nullable Boolean locationTracking, @Nullable Boolean shouldSetWhat) {
        this.shouldSetWhat = shouldSetWhat;
        this.locationTracking = locationTracking;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        doReturn(mRegularDispatcherComponent).when(mClientUnit).getComponent();
        doReturn(new CommutationComponentId(contextRule.getContext().getPackageName()))
            .when(mRegularDispatcherComponent).getComponentId();
        mHandler = new UpdatePreActivationConfigHandler(mRegularDispatcherComponent, mRestrictionController);
        when(mCounterConfiguration.isLocationTrackingEnabled()).thenReturn(locationTracking);
        doReturn(new CommonArguments.ReporterArguments(mCounterConfiguration, null))
            .when(mRegularDispatcherComponent).getConfiguration();
    }

    @Test
    public void test() {
        mHandler.process(mCounterReport, mClientUnit);
        if (shouldSetWhat != null) {
            verify(GlobalServiceLocator.getInstance().getLocationClientApi())
                .updateTrackingStatusFromClient(shouldSetWhat);
        } else {
            verify(GlobalServiceLocator.getInstance().getLocationClientApi(), never())
                .updateTrackingStatusFromClient(anyBoolean());
        }
    }
}
