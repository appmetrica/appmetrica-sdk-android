package io.appmetrica.analytics.impl.component.processor.event;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.impl.AppStandbyBucketConverter;
import io.appmetrica.analytics.impl.AvailableProvidersRetriever;
import io.appmetrica.analytics.impl.BackgroundRestrictionsState;
import io.appmetrica.analytics.impl.BackgroundRestrictionsStateProvider;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.EventSaver;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.db.protobuf.ProtobufStateStorageImpl;
import io.appmetrica.analytics.impl.permissions.AppPermissionsState;
import io.appmetrica.analytics.impl.permissions.PermissionsChecker;
import io.appmetrica.analytics.impl.protobuf.client.AppPermissionsStateProtobuf;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReportPermissionHandlerTest extends CommonTest {

    @Mock
    private ComponentUnit mComponent;
    @Mock
    private VitalComponentDataProvider vitalComponentDataProvider;
    @Mock
    private PermissionsChecker mPermissionsChecker;
    @Mock
    private EventSaver mEventSaver;
    @Mock
    private BackgroundRestrictionsStateProvider mBackgroundRestrictionsStateProvider;
    @Mock
    private AppStandbyBucketConverter mAppStandbyBucketConverter;
    @Mock
    private AvailableProvidersRetriever mAvailableProvidersRetriever;
    private BackgroundRestrictionsState mOldBgRestrictionState;
    private BackgroundRestrictionsState mOldBgRestrictionStateCopy;
    private AppPermissionsState mOldAppPermissionsState;
    private List<PermissionState> mocks;
    private ReportPermissionHandler mReportPermissionsHandler;
    private List<PermissionState> mOldPermissions;
    private List<String> mOldProviders;
    private List<String> mOldProvidersCopy;

    @Mock
    private ProtobufStateStorageImpl<AppPermissionsState, AppPermissionsStateProtobuf.AppPermissionsState> mPermissionsStorage;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mOldPermissions = Arrays.asList(
            new PermissionState("internet", true),
            new PermissionState("wifi_state", false)
        );
        mOldProviders = Arrays.asList("gps", "network");
        mOldProvidersCopy = Arrays.asList("gps", "network");
        mOldBgRestrictionState = new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.ACTIVE, false);
        mOldBgRestrictionStateCopy = new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.ACTIVE, false);
        mOldAppPermissionsState = new AppPermissionsState(mOldPermissions, mOldBgRestrictionState, mOldProviders);
        when(mComponent.getEventSaver()).thenReturn(mEventSaver);
        when(mComponent.getVitalComponentDataProvider()).thenReturn(vitalComponentDataProvider);
        when(mPermissionsStorage.read()).thenReturn(mOldAppPermissionsState);
        when(mAvailableProvidersRetriever.getAvailableProviders()).thenReturn(mOldProvidersCopy);
        ComponentId id = mock(ComponentId.class);
        doReturn(id).when(mComponent).getComponentId();
        doReturn(RuntimeEnvironment.getApplication()).when(mComponent).getContext();
        mReportPermissionsHandler = new ReportPermissionHandler(
            mComponent,
            mPermissionsChecker,
            mPermissionsStorage,
            mBackgroundRestrictionsStateProvider,
            mAppStandbyBucketConverter,
            mAvailableProvidersRetriever
        );
    }

    @Test
    public void testEmptyPermissionsSent() {
        mocks = new ArrayList<PermissionState>();
        doReturn(true).when(mComponent).needToCheckPermissions();
        doReturn(mocks).when(mPermissionsChecker).check(any(Context.class), any(List.class));
        doReturn(true).when(vitalComponentDataProvider).isFirstEventDone();
        CounterReport report = mock(CounterReport.class);

        mReportPermissionsHandler.process(report);

        verify(mEventSaver, times(1)).savePermissionsReport(any(CounterReport.class));
        verify(mPermissionsStorage, times(1)).save(argThat(matches(mocks, null, mOldProviders)));
    }

    @Test
    public void testNullBgRestrictions() {
        mocks = Arrays.asList(new PermissionState("1", false), new PermissionState("2", true));
        doReturn(true).when(mComponent).needToCheckPermissions();
        doReturn(mocks).when(mPermissionsChecker).check(any(Context.class), any(List.class));
        doReturn(null).when(mBackgroundRestrictionsStateProvider).getBackgroundRestrictionsState();
        doReturn(true).when(vitalComponentDataProvider).isFirstEventDone();
        CounterReport report = mock(CounterReport.class);

        mReportPermissionsHandler.process(report);

        verify(mEventSaver, times(1)).savePermissionsReport(any(CounterReport.class));
        verify(mPermissionsStorage, times(1)).save(argThat(matches(mocks, null, mOldProviders)));
    }

    @Test
    public void testPermissionsChangedAndBgRestrictionsNot() {
        mocks = Arrays.asList(new PermissionState("1", false), new PermissionState("2", true));
        doReturn(true).when(mComponent).needToCheckPermissions();
        doReturn(mocks).when(mPermissionsChecker).check(any(Context.class), any(List.class));
        doReturn(mOldBgRestrictionStateCopy).when(mBackgroundRestrictionsStateProvider).getBackgroundRestrictionsState();
        doReturn(true).when(vitalComponentDataProvider).isFirstEventDone();
        CounterReport report = mock(CounterReport.class);

        mReportPermissionsHandler.process(report);

        verify(mEventSaver, times(1)).savePermissionsReport(any(CounterReport.class));
        verify(mPermissionsStorage, times(1)).save(argThat(matches(mocks, mOldBgRestrictionState, mOldProviders)));
    }

    @Test
    public void testBgRestrictionsChangedAndPermissionsNot() {
        doReturn(true).when(mComponent).needToCheckPermissions();
        doReturn(null).when(mPermissionsChecker).check(any(Context.class), any(List.class));
        final BackgroundRestrictionsState newBackgroundRestrictionsState =
            new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.FREQUENT, false);
        doReturn(newBackgroundRestrictionsState).when(mBackgroundRestrictionsStateProvider).getBackgroundRestrictionsState();
        doReturn(true).when(vitalComponentDataProvider).isFirstEventDone();
        CounterReport report = mock(CounterReport.class);

        mReportPermissionsHandler.process(report);

        verify(mEventSaver, times(1)).savePermissionsReport(any(CounterReport.class));
        verify(mPermissionsStorage, times(1)).save(argThat(matches(mOldPermissions, newBackgroundRestrictionsState, mOldProviders)));
    }

    @Test
    public void testPermissionsAndBgRestrictionsNotUpdatedAndShouldForceSend() {
        doReturn(true).when(mComponent).needToCheckPermissions();
        doReturn(true).when(mComponent).shouldForceSendPermissions();
        doReturn(true).when(vitalComponentDataProvider).isFirstEventDone();
        doReturn(null).when(mPermissionsChecker).check(any(Context.class), any(List.class));
        doReturn(mOldBgRestrictionStateCopy).when(mBackgroundRestrictionsStateProvider).getBackgroundRestrictionsState();
        CounterReport report = mock(CounterReport.class);

        mReportPermissionsHandler.process(report);

        verify(mEventSaver).savePermissionsReport(any(CounterReport.class));
        verify(mPermissionsStorage, never()).save(any(AppPermissionsState.class));
    }

    @Test
    public void testPermissionsAndBgRestrictionsNotUpdatedAndShouldNotForceSend() {
        doReturn(true).when(mComponent).needToCheckPermissions();
        doReturn(false).when(mComponent).shouldForceSendPermissions();
        doReturn(true).when(vitalComponentDataProvider).isFirstEventDone();
        doReturn(null).when(mPermissionsChecker).check(any(Context.class), any(List.class));
        doReturn(mOldBgRestrictionStateCopy).when(mBackgroundRestrictionsStateProvider).getBackgroundRestrictionsState();
        CounterReport report = mock(CounterReport.class);

        mReportPermissionsHandler.process(report);

        verify(mEventSaver, never()).savePermissionsReport(any(CounterReport.class));
        verify(mPermissionsStorage, never()).save(any(AppPermissionsState.class));
    }

    @Test
    public void testBackgroundRestrictions() {
        doReturn(true).when(vitalComponentDataProvider).isFirstEventDone();
        mocks = Arrays.asList(new PermissionState("1", false), new PermissionState("2", true));
        doReturn(true).when(mComponent).needToCheckPermissions();
        doReturn(mocks).when(mPermissionsChecker).check(any(Context.class), any(List.class));
        doReturn(new BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.RARE, false)).when(mBackgroundRestrictionsStateProvider).getBackgroundRestrictionsState();
        when(mAppStandbyBucketConverter.fromAppStandbyBucketToString(BackgroundRestrictionsState.AppStandByBucket.RARE)).thenReturn("rare");
        mReportPermissionsHandler.process(new CounterReport());
        ArgumentCaptor<CounterReport> captor = ArgumentCaptor.forClass(CounterReport.class);
        verify(mEventSaver).savePermissionsReport(captor.capture());
        assertThat(captor.getValue().getValue()).contains("\"background_restrictions\":{\"background_restricted\":false,\"app_standby_bucket\":\"rare\"}");
    }

    @Test
    public void testOnlyProvidersChanged() {
        final List<String> newProviders = Arrays.asList("passive", "gps");
        doReturn(newProviders).when(mAvailableProvidersRetriever).getAvailableProviders();
        doReturn(true).when(vitalComponentDataProvider).isFirstEventDone();
        doReturn(true).when(mComponent).needToCheckPermissions();
        doReturn(null).when(mPermissionsChecker).check(any(Context.class), any(List.class));
        doReturn(mOldBgRestrictionState).when(mBackgroundRestrictionsStateProvider).getBackgroundRestrictionsState();
        mReportPermissionsHandler.process(new CounterReport());
        verify(mEventSaver).savePermissionsReport(any(CounterReport.class));
        verify(mPermissionsStorage).save(argThat(matches(mOldPermissions, mOldBgRestrictionState, newProviders)));
    }

    private ArgumentMatcher<AppPermissionsState> matches(final List<PermissionState> permissions, final BackgroundRestrictionsState bgRestrictions, final List<String> providers) {
        return new ArgumentMatcher<AppPermissionsState>() {
            @Override
            public boolean matches(AppPermissionsState argument) {
                return CollectionUtils.areCollectionsEqual(argument.mAvailableProviders, providers) &&
                    CollectionUtils.areCollectionsEqual(argument.mPermissionStateList, permissions) &&
                    Utils.areEqual(argument.mBackgroundRestrictionsState, bgRestrictions);
            }
        };
    }
}
