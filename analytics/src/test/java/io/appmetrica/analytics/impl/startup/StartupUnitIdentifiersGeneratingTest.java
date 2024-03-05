package io.appmetrica.analytics.impl.startup;

import android.content.Context;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.impl.ClidsInfoStorage;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.startup.uuid.UuidValidator;
import io.appmetrica.analytics.impl.utils.DeviceIdGenerator;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class StartupUnitIdentifiersGeneratingTest extends CommonTest {

    private Context mContext;
    @Mock
    private StartupConfigurationHolder mStartupConfigurationHolder;
    @Mock
    private ComponentId mComponentId;
    @Mock
    private StartupResultListener mStartupResultListener;
    @Mock
    private StartupState.Storage mStorage;
    @Mock
    private DeviceIdGenerator deviceIdGenerator;
    @Mock
    private StartupRequestConfig mStartupRequestConfig;
    @Mock
    private AdvertisingIdsHolder mAdvertisingIdsHolder;
    @Mock
    private ClidsInfoStorage clidsStorage;
    @Mock
    private ClidsStateChecker clidsStateChecker;
    @Mock
    private MultiProcessSafeUuidProvider uuidProvider;
    @Mock
    private UuidValidator uuidValidator;

    private StartupState mStartupState;
    private String mGeneratedUuid = "generated uuid";
    private String invalidUuid = "invalid uuid";
    private String mGeneratedDeviceId = "generated deviceid";

    @Rule
    public final GlobalServiceLocatorRule mGlobalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        when(mStartupRequestConfig.getAdvertisingIdsHolder()).thenReturn(mAdvertisingIdsHolder);
        when(uuidProvider.readUuid()).thenReturn(new IdentifiersResult(mGeneratedUuid, IdentifierStatus.OK, null));
        when(uuidValidator.isValid(mGeneratedUuid)).thenReturn(true);
        when(uuidValidator.isValid(invalidUuid)).thenReturn(false);
    }

    @Test
    public void testUuidGeneratedIfEmpty() {
        checkUuidGenerated("");
    }

    @Test
    public void testUuidGeneratedIfNull() {
        checkUuidGenerated(null);
    }

    @Test
    public void uuidGeneratedIfInvalid() {
        checkUuidGenerated(invalidUuid);
    }

    @Test
    public void testDeviceIdGeneratedIfEmpty() {
        checkDeviceIdGenerated("");
    }

    @Test
    public void testDeviceIdGeneratedIfNull() {
        checkDeviceIdGenerated(null);
    }

    @Test
    public void testDeviceIdIsNotUpdated() {
        when(deviceIdGenerator.generateDeviceId()).thenReturn(mGeneratedDeviceId);
        createStartupUnit("uuid", "");
        ArgumentCaptor<StartupState> captor = ArgumentCaptor.forClass(StartupState.class);
        verify(mStorage).save(captor.capture());
        StartupState startupState = captor.getValue();
        assertThat(startupState.getDeviceId()).isEqualTo(mGeneratedDeviceId);

        createStartupUnit("uuid", mGeneratedDeviceId);
        verify(mStorage, times(2)).save(captor.capture());
        startupState = captor.getValue();
        assertThat(startupState.getDeviceId()).isEqualTo(mGeneratedDeviceId);
    }

    private void checkDeviceIdGenerated(@Nullable String deviceId) {
        when(deviceIdGenerator.generateDeviceId()).thenReturn(mGeneratedDeviceId);
        createStartupUnit("uuid", deviceId);
        verify(deviceIdGenerator).generateDeviceId();
        ArgumentCaptor<StartupState> captor = ArgumentCaptor.forClass(StartupState.class);
        verify(mStorage).save(captor.capture());
        StartupState startupState = captor.getValue();
        assertThat(startupState.getDeviceId()).isEqualTo(mGeneratedDeviceId);
        assertThat(startupState.getDeviceIdHash()).isEmpty();
        verify(mStartupConfigurationHolder).updateStartupState(startupState);
    }

    private void checkUuidGenerated(@Nullable String uuid) {
        createStartupUnit(uuid, "deviceid");
        verify(uuidProvider).readUuid();
        ArgumentCaptor<StartupState> captor = ArgumentCaptor.forClass(StartupState.class);
        verify(mStorage).save(captor.capture());
        StartupState startupState = captor.getValue();
        assertThat(startupState.getUuid()).isEqualTo(mGeneratedUuid);
        verify(mStartupConfigurationHolder).updateStartupState(startupState);
    }

    private void createStartupUnit(@Nullable String uuid, @Nullable String deviceId) {
        mStartupState = TestUtils.createDefaultStartupStateBuilder()
                .withUuid(uuid)
                .withDeviceId(deviceId)
                .build();
        StartupUnit startupUnit = new StartupUnit(
                mContext, mComponentId, mStartupResultListener, mStorage, mStartupState, deviceIdGenerator,
                mStartupConfigurationHolder, new SystemTimeProvider(), clidsStorage, clidsStateChecker,
                uuidProvider, uuidValidator
        );
    }

}
