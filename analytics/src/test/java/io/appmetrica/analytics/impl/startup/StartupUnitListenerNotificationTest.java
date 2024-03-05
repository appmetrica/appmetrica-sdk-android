package io.appmetrica.analytics.impl.startup;

import io.appmetrica.analytics.coreutils.internal.time.TimeProvider;
import io.appmetrica.analytics.impl.ClidsInfoStorage;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.impl.startup.parsing.StartupResult;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.startup.uuid.UuidValidator;
import io.appmetrica.analytics.impl.utils.DeviceIdGenerator;
import io.appmetrica.analytics.impl.utils.ServerTime;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class StartupUnitListenerNotificationTest extends CommonTest {

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Mock
    private StartupConfigurationHolder mStartupConfigurationHolder;
    @Mock
    private DeviceIdGenerator deviceIdGenerator;
    @Mock
    private StartupRequestConfig mStartupRequestConfig;
    @Mock
    private StartupResultListener mStartupResultListener;
    @Mock
    private ComponentId mComponentId;
    @Mock
    private StartupState.Storage mStartupStateStorage;
    @Mock
    private ClidsInfoStorage clidsStorage;
    @Mock
    private ClidsStateChecker clidsStateChecker;
    @Mock
    private MultiProcessSafeUuidProvider uuidProvider;
    @Mock
    private UuidValidator uuidValidator;
    private final String mPackageName = "test.package";
    private StartupUnit mStartupUnit;
    private StartupState mStartupState;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(mStartupConfigurationHolder.get()).thenReturn(mStartupRequestConfig);
        when(mComponentId.getPackage()).thenReturn(mPackageName);
    }

    @After
    public void tearDown() {
        GlobalServiceLocator.destroy();
    }

    @Test
    public void testGeneratedIdentifiersNotifies() {
        createStartupUnitToModifyStartup();
        checkListenersNotified(1);
    }

    @Test
    public void testDidNotGenerateIdentifiersButConstructorNotifies() {
        createStartupUnitToNotModifyStartup();
        checkListenersNotified(1);
    }

    @Test
    public void testOnRequestCompleteNotifies() {
        createStartupUnitToNotModifyStartup();
        checkListenersNotified(1);
        StartupResult startupResult = mock(StartupResult.class);
        ServerTime.getInstance().init();
        when(startupResult.getValidTimeDifference()).thenReturn(100L);
        when(startupResult.getCollectionFlags()).thenReturn(mock(CollectingFlags.class));
        when(mStartupRequestConfig.getChosenClids()).thenReturn(mock(ClidsInfo.Candidate.class));
        when(mStartupConfigurationHolder.getStartupState()).thenReturn(mStartupState);
        mStartupUnit.onRequestComplete(startupResult, mStartupRequestConfig, null);
        checkListenersNotified(2);
    }

    @Test
    public void testUpdateConfigurationNotifiesListener() {
        createStartupUnitToNotModifyStartup();
        checkListenersNotified(1);

        when(mStartupConfigurationHolder.getStartupState()).thenReturn(mStartupState);
        StartupRequestConfig.Arguments arguments = mock(StartupRequestConfig.Arguments.class);
        when(mStartupRequestConfig.getNewCustomHosts()).thenReturn(null);
        when(mStartupRequestConfig.getStartupHostsFromClient()).thenReturn(Arrays.asList("host"));
        when(mStartupRequestConfig.hasNewCustomHosts()).thenReturn(true);
        mStartupUnit.updateConfiguration(arguments);
        checkListenersNotified(2);
    }

    private void checkListenersNotified(int times) {
        List<Object> objects = new ArrayList<Object>();
        for (int i = 0; i < times; i++) {
            objects.add(mStartupConfigurationHolder);
            objects.add(GlobalServiceLocator.getInstance().getStartupStateHolder());
            objects.add(mStartupResultListener);
        }
        InOrder inOrder = Mockito.inOrder(objects.toArray());
        for (int i = 0; i < times; i++) {
            inOrder.verify(mStartupConfigurationHolder).updateStartupState(any(StartupState.class));
            inOrder.verify(GlobalServiceLocator.getInstance().getStartupStateHolder()).onStartupStateChanged(any(StartupState.class));
            inOrder.verify(mStartupResultListener).onStartupChanged(eq(mPackageName), any(StartupState.class));
        }
    }

    private void createStartupUnitToModifyStartup() {
        mStartupState = TestUtils.createDefaultStartupStateBuilder()
                .withUuid("uuid")
                .withDeviceId("deviceId")
                .build();
        when(uuidValidator.isValid("uuid")).thenReturn(true);
        when(mStartupConfigurationHolder.get()).thenReturn(mStartupRequestConfig);
        when(deviceIdGenerator.generateDeviceId()).thenReturn("deviceid");
        mStartupUnit = new StartupUnit(
                RuntimeEnvironment.getApplication(), mComponentId, mStartupResultListener, mStartupStateStorage,
                mStartupState, deviceIdGenerator, mStartupConfigurationHolder, mock(TimeProvider.class),
                clidsStorage, clidsStateChecker, uuidProvider, uuidValidator);
    }

    private void createStartupUnitToNotModifyStartup() {
        when(mStartupConfigurationHolder.get()).thenReturn(mStartupRequestConfig);
        mStartupState = TestUtils.createDefaultStartupStateBuilder()
                .withUuid("uuid")
                .withDeviceId("deviceId")
                .build();
        when(uuidValidator.isValid("uuid")).thenReturn(true);
        when(mStartupConfigurationHolder.get()).thenReturn(mStartupRequestConfig);
        mStartupUnit = new StartupUnit(
                RuntimeEnvironment.getApplication(), mComponentId, mStartupResultListener, mStartupStateStorage,
                mStartupState, deviceIdGenerator, mStartupConfigurationHolder, mock(TimeProvider.class),
                clidsStorage, clidsStateChecker, uuidProvider, uuidValidator);
    }
}
