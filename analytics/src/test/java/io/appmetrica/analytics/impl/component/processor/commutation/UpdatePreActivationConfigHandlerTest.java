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
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class UpdatePreActivationConfigHandlerTest extends CommonTest {

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

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
    private UpdatePreActivationConfigHandler mHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        doReturn(mRegularDispatcherComponent).when(mClientUnit).getComponent();
        doReturn(new CommutationComponentId(RuntimeEnvironment.getApplication().getPackageName())).when(mRegularDispatcherComponent).getComponentId();
        mHandler = new UpdatePreActivationConfigHandler(mRegularDispatcherComponent, mRestrictionController);
    }

    @Test
    public void testRestriction() {
        doReturn(null).when(mCounterConfiguration).getDataSendingEnabled();
        doReturn(new CommonArguments.ReporterArguments(mCounterConfiguration, null)).when(mRegularDispatcherComponent).getConfiguration();
        mHandler.process(mCounterReport, mClientUnit);
        verify(mRestrictionController).setEnabledFromMainReporter(null);
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class LocationTest {

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
        private UpdatePreActivationConfigHandler mHandler;

        private final boolean mShouldSet;
        @Nullable
        private final Boolean mShouldSetWhat;

        @ParameterizedRobolectricTestRunner.Parameters(name = "locationTracking = {0}, allowedByBridge = {1}")
        public static Collection<Object[]> getData() {
            return Arrays.asList(
                    new Object[]{null, false, null},
                    new Object[]{false, true, false},
                    new Object[]{true, true, true}
            );
        }

        public LocationTest(@Nullable Boolean locationTracking, boolean shouldSet, @Nullable Boolean shouldSetWhat) {
            MockitoAnnotations.openMocks(this);
            doReturn(mRegularDispatcherComponent).when(mClientUnit).getComponent();
            doReturn(new CommutationComponentId(RuntimeEnvironment.getApplication().getPackageName())).when(mRegularDispatcherComponent).getComponentId();
            mHandler = new UpdatePreActivationConfigHandler(mRegularDispatcherComponent, mRestrictionController);
            when(mCounterConfiguration.isLocationTrackingEnabled()).thenReturn(locationTracking);
            doReturn(new CommonArguments.ReporterArguments(mCounterConfiguration, null)).when(mRegularDispatcherComponent).getConfiguration();
            mShouldSet = shouldSet;
            mShouldSetWhat = shouldSetWhat;
        }

        @Test
        public void test() {
            mHandler.process(mCounterReport, mClientUnit);
            if (mShouldSet) {
                verify(GlobalServiceLocator.getInstance().getLocationClientApi())
                    .updateTrackingStatusFromClient(mShouldSetWhat);
            } else {
                verify(GlobalServiceLocator.getInstance().getLocationClientApi(), never())
                    .updateTrackingStatusFromClient(anyBoolean());
            }
        }
    }
}
