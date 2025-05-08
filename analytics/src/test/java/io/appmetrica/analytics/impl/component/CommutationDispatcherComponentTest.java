package io.appmetrica.analytics.impl.component;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.ClientIdentifiersHolder;
import io.appmetrica.analytics.impl.ClientIdentifiersProvider;
import io.appmetrica.analytics.impl.ClientIdentifiersProviderFactory;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.IdentifiersData;
import io.appmetrica.analytics.impl.TaskProcessor;
import io.appmetrica.analytics.impl.component.clients.CommutationClientUnit;
import io.appmetrica.analytics.impl.component.processor.commutation.CommutationHandler;
import io.appmetrica.analytics.impl.component.processor.commutation.CommutationReportProcessor;
import io.appmetrica.analytics.impl.referrer.common.ReferrerChosenListener;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.impl.referrer.service.ReferrerHolder;
import io.appmetrica.analytics.impl.referrer.service.ReferrerManager;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.impl.startup.Constants;
import io.appmetrica.analytics.impl.startup.StartupCenter;
import io.appmetrica.analytics.impl.startup.StartupError;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.StartupUnit;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.internal.CounterConfigurationReporterType;
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(RobolectricTestRunner.class)
public class CommutationDispatcherComponentTest extends CommonTest {

    private Context mContext;
    private CommutationDispatcherComponent mComponentUnit;
    @Mock
    private CommutationDispatcherComponentFieldsFactory mFieldsFactory;
    @Mock
    private IdentifiersData mIdentifiersData;
    @Mock
    private ResultReceiver mResultReceiver;
    @Mock
    private CommonArguments.ReporterArguments mReporterArguments;
    @Mock
    private StartupRequestConfig.Arguments mStartupArguments;
    @Mock
    private ReporterArgumentsHolder mReporterArgumentsHolder;
    @Mock
    private StartupCenter mStartupCenter;
    @Mock
    private StartupUnit mStartupUnit;
    @Mock
    private ReferrerHolder mReferrerHolder;
    @Mock
    private ComponentLifecycleManager<CommutationClientUnit> mLifecycleManager;
    @Mock
    private CommutationReportProcessor<CommutationHandler, CommutationDispatcherComponent> mCommutationReportProcessor;
    @Mock
    private TaskProcessor<CommutationDispatcherComponent> mTaskProcessor;
    @Mock
    private ClientIdentifiersProviderFactory mClientIdentifiersProviderFactory;
    @Mock
    private ClientIdentifiersProvider mClientIdentifiersProvider;
    @Mock
    private ClientIdentifiersHolder mClientIdentifiersHolder;
    private StartupState mStartupState;
    @Mock
    private ReferrerManager referrerManager;
    private ComponentId mComponentId;
    private CommonArguments mCommonArguments;
    private Map<String, String> mClidsForVerification;
    private Map<String, String> lastClientClidsForRequest;
    private final List<String> mIdentifiers = Arrays.asList(
        Constants.StartupParamsCallbackKeys.UUID,
        Constants.StartupParamsCallbackKeys.DEVICE_ID
    );

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();

        lastClientClidsForRequest = new HashMap<String, String>();
        lastClientClidsForRequest.put("clid2", "2");
        lastClientClidsForRequest.put("clid3", "3");
        mStartupState = createStartupWithClientClids(lastClientClidsForRequest);
        when(mStartupUnit.getStartupState()).thenReturn(mStartupState);
        when(mClientIdentifiersProviderFactory.createClientIdentifiersProvider(
            mStartupUnit,
            GlobalServiceLocator.getInstance().getAdvertisingIdGetter(),
            mContext
        )).thenReturn(mClientIdentifiersProvider);
        mClidsForVerification = new HashMap<String, String>();
        mClidsForVerification.put("clid0", "0");
        mClidsForVerification.put("clid1", "1");
        when(mClientIdentifiersProvider.createClientIdentifiersHolder(nullable(Map.class))).thenReturn(mock(ClientIdentifiersHolder.class));
        mComponentId = new CommutationComponentId(mContext.getPackageName());
        mCommonArguments = new CommonArguments(mStartupArguments, mReporterArguments, null);
        when(mIdentifiersData.getResultReceiver()).thenReturn(mResultReceiver);
        when(mIdentifiersData.getIdentifiersList()).thenReturn(mIdentifiers);
        when(mIdentifiersData.getClidsFromClientForVerification()).thenReturn(mClidsForVerification);
        when(mStartupCenter.getOrCreateStartupUnit(any(Context.class), any(ComponentId.class), any(StartupRequestConfig.Arguments.class))).thenReturn(mStartupUnit);
        when(mFieldsFactory.createCommutationReportProcessor(any(CommutationDispatcherComponent.class))).thenReturn(mCommutationReportProcessor);
        when(mFieldsFactory.createTaskProcessor(any(CommutationDispatcherComponent.class), same(mStartupUnit))).thenReturn(mTaskProcessor);
        mComponentUnit = new CommutationDispatcherComponent(
            mContext,
            mStartupCenter,
            mComponentId,
            mCommonArguments,
            mReporterArgumentsHolder,
            mReferrerHolder,
            mLifecycleManager,
            mFieldsFactory,
            mClientIdentifiersProviderFactory,
            referrerManager
        );
    }

    @Test
    public void testObjectCreation() {
        ArgumentCaptor<CommutationDispatcherComponent> componentCaptor = ArgumentCaptor.forClass(CommutationDispatcherComponent.class);
        verify(mFieldsFactory).createCommutationReportProcessor(componentCaptor.capture());
        verify(mFieldsFactory).createTaskProcessor(componentCaptor.capture(), same(mStartupUnit));
        for (CommutationDispatcherComponent component : componentCaptor.getAllValues()) {
            assertThat(component).isSameAs(mComponentUnit);
        }
        verify(GlobalServiceLocator.getInstance().getAdvertisingIdGetter()).updateStateFromClientConfig(true);
        verify(mStartupCenter).registerStartupListener(mComponentId, mComponentUnit);
    }

    @Test
    public void testUpdateSdkConfig() {
        CommonArguments.ReporterArguments reporterArguments = mock(CommonArguments.ReporterArguments.class);
        mComponentUnit.updateSdkConfig(reporterArguments);
        verify(mReporterArgumentsHolder).updateArguments(reporterArguments);
    }

    @Test
    public void testGetReporterType() {
        assertThat(mComponentUnit.getReporterType()).isEqualTo(CounterConfigurationReporterType.COMMUTATION);
    }

    @Test
    public void testRegisterListener() {
        Map<String, String> newClientClidsForRequest = new HashMap<String, String>();
        newClientClidsForRequest.put("newClid0", "0");
        StartupState startupState = createStartupWithClientClids(newClientClidsForRequest);
        when(mClientIdentifiersProvider.createClientIdentifiersHolder(newClientClidsForRequest)).thenReturn(mClientIdentifiersHolder);
        mComponentUnit.onStartupChanged(startupState);
        when(mStartupUnit.getStartupState()).thenReturn(startupState);
        CommutationClientUnit listener = mock(CommutationClientUnit.class);
        mComponentUnit.connectClient(listener);
        verify(mLifecycleManager).connectClient(listener);
        verify(listener).onClientIdentifiersChanged(mClientIdentifiersHolder);
    }

    @Test
    public void testDisconnectClient() {
        CommutationClientUnit listener = mock(CommutationClientUnit.class);
        mComponentUnit.disconnectClient(listener);
        verify(mLifecycleManager).disconnectClient(listener);
    }

    @Test
    public void testHandleReport() {
        CommutationClientUnit clientUnit = mock(CommutationClientUnit.class);
        CounterReport report = mock(CounterReport.class);
        mComponentUnit.handleReport(report, clientUnit);
        verify(mCommutationReportProcessor).process(report, clientUnit);
    }

    @Test
    public void testGetConfiguration() {
        CommonArguments.ReporterArguments reporterArguments = mock(CommonArguments.ReporterArguments.class);
        when(mReporterArgumentsHolder.getArguments()).thenReturn(reporterArguments);
        assertThat(mComponentUnit.getConfiguration()).isEqualTo(reporterArguments);
    }

    @Test
    public void testGetComponentId() {
        assertThat(mComponentUnit.getComponentId()).isEqualTo(mComponentId);
    }

    @Test
    public void testOnStartupChangedNotifiesListeners() {
        CommutationClientUnit listener1 = mock(CommutationClientUnit.class);
        CommutationClientUnit listener2 = mock(CommutationClientUnit.class);
        when(mLifecycleManager.getConnectedClients()).thenReturn(Arrays.asList(listener1, listener2));
        Map<String, String> newClientClidsForRequest = new HashMap<String, String>();
        newClientClidsForRequest.put("newClid0", "0");
        when(mClientIdentifiersProvider.createClientIdentifiersHolder(newClientClidsForRequest)).thenReturn(mClientIdentifiersHolder);
        StartupState startupState = createStartupWithClientClids(newClientClidsForRequest);
        mComponentUnit.onStartupChanged(startupState);
        verify(listener1).onClientIdentifiersChanged(mClientIdentifiersHolder);
        verify(listener2).onClientIdentifiersChanged(mClientIdentifiersHolder);
    }

    @Test
    public void testGetContext() {
        assertThat(mComponentUnit.getContext()).isSameAs(mContext);
    }

    @Test
    public void testGetReferrerHolder() {
        assertThat(mComponentUnit.getReferrerHolder()).isSameAs(mReferrerHolder);
    }

    @Test
    public void testUpdateConfig() {
        mComponentUnit.updateConfig(mCommonArguments);
        verify(mStartupUnit).updateConfiguration(mStartupArguments);
        verify(mReporterArgumentsHolder).updateArguments(mReporterArguments);
    }

    @Test
    public void testProvokeStartupOrGetCurrentStateNotRequired() {
        when(mClientIdentifiersProvider.createClientIdentifiersHolder(mClidsForVerification)).thenReturn(mClientIdentifiersHolder);
        when(mStartupUnit.isStartupRequired()).thenReturn(false);
        when(mStartupUnit.isStartupRequired(mIdentifiers, mClidsForVerification)).thenReturn(false);
        when(mStartupUnit.getStartupState()).thenReturn(TestUtils.createDefaultStartupState());
        mComponentUnit.provokeStartupOrGetCurrentState(mIdentifiersData);
        verify(mClientIdentifiersHolder).toBundle(any(Bundle.class));
        verify(mResultReceiver).send(eq(1), any(Bundle.class));
    }

    @Test
    public void testProvokeStartupOrGetCurrentStateRequiredOnlyForIdentifiers() {
        when(mClientIdentifiersProvider.createClientIdentifiersHolder(mClidsForVerification)).thenReturn(mClientIdentifiersHolder);
        when(mStartupUnit.isStartupRequired()).thenReturn(false);
        when(mStartupUnit.isStartupRequired(mIdentifiers, mClidsForVerification)).thenReturn(true);
        when(mStartupUnit.getStartupState()).thenReturn(TestUtils.createDefaultStartupState());
        mComponentUnit.provokeStartupOrGetCurrentState(mIdentifiersData);
        verify(mClientIdentifiersHolder).toBundle(any(Bundle.class));
        verify(mResultReceiver).send(eq(1), any(Bundle.class));
    }

    @Test
    public void testProvokeStartupOrGetCurrentStateNotRequiredForIdentifiersButRequired() {
        when(mClientIdentifiersProvider.createClientIdentifiersHolder(mClidsForVerification)).thenReturn(mClientIdentifiersHolder);
        when(mStartupUnit.isStartupRequired()).thenReturn(true);
        when(mStartupUnit.isStartupRequired(mIdentifiers, mClidsForVerification)).thenReturn(false);
        when(mStartupUnit.getStartupState()).thenReturn(TestUtils.createDefaultStartupState());
        mComponentUnit.provokeStartupOrGetCurrentState(mIdentifiersData);
        verify(mClientIdentifiersHolder).toBundle(any(Bundle.class));
        verify(mResultReceiver).send(eq(1), any(Bundle.class));
        verify(mTaskProcessor).flushAllTasks();
    }

    @Test
    public void testProvokeStartupOrGetCurrentStateNotRequiredForIdentifiersAndInGeneral() {
        when(mClientIdentifiersProvider.createClientIdentifiersHolder(mClidsForVerification)).thenReturn(mClientIdentifiersHolder);
        when(mStartupUnit.isStartupRequired()).thenReturn(true);
        when(mStartupUnit.isStartupRequired(mIdentifiers, mClidsForVerification)).thenReturn(true);
        when(mStartupUnit.getStartupState()).thenReturn(TestUtils.createDefaultStartupState());
        mComponentUnit.provokeStartupOrGetCurrentState(mIdentifiersData);
        verify(mResultReceiver, never()).send(anyInt(), any(Bundle.class));
        verify(mTaskProcessor).flushAllTasks();
    }

    @Test
    public void testOnStartupChangedNotifiesIfDataConsistent() {
        StartupState newStartupState = createStartupWithClientClids(null);
        when(mClientIdentifiersProvider.createClientIdentifiersHolder(mClidsForVerification)).thenReturn(mClientIdentifiersHolder);
        setUpToRequireStartup();
        verify(mResultReceiver, never()).send(anyInt(), any(Bundle.class));
        when(mIdentifiersData.isStartupConsistent(same(newStartupState))).thenReturn(true);
        mComponentUnit.onStartupChanged(newStartupState);
        verify(mClientIdentifiersHolder).toBundle(any(Bundle.class));
        verify(mResultReceiver).send(eq(1), any(Bundle.class));
    }

    @Test
    public void testOnStartupChangedResendsStartupIfDataNotConsistent() {
        StartupState newStartupState = createStartupWithClientClids(null);
        when(mClientIdentifiersProvider.createClientIdentifiersHolder(mClidsForVerification)).thenReturn(mClientIdentifiersHolder);
        setUpToRequireStartup();
        verify(mTaskProcessor).flushAllTasks();
        verify(mResultReceiver, never()).send(anyInt(), any(Bundle.class));
        when(mIdentifiersData.isStartupConsistent(same(newStartupState))).thenReturn(false);
        mComponentUnit.onStartupChanged(newStartupState);
        verify(mResultReceiver, never()).send(anyInt(), any(Bundle.class));
        verify(mTaskProcessor, times(2)).flushAllTasks();

        when(mIdentifiersData.isStartupConsistent(same(newStartupState))).thenReturn(true);
        mComponentUnit.onStartupChanged(newStartupState);
        verify(mClientIdentifiersHolder).toBundle(any(Bundle.class));
        verify(mResultReceiver).send(eq(1), any(Bundle.class));
    }

    @Test
    public void testOnStartupError() { // also that it clears
        StartupError startupError = StartupError.PARSE;
        StartupState newStartupState = createStartupWithClientClids(null);
        when(mClientIdentifiersProvider.createClientIdentifiersHolder(mClidsForVerification)).thenReturn(mClientIdentifiersHolder);
        setUpToRequireStartup();
        verify(mResultReceiver, never()).send(anyInt(), any(Bundle.class));
        mComponentUnit.onStartupError(startupError, newStartupState);
        verify(mClientIdentifiersHolder).toBundle(any(Bundle.class));
        verify(mResultReceiver).send(eq(2), any(Bundle.class));

        when(mIdentifiersData.isStartupConsistent(same(newStartupState))).thenReturn(true);
        mComponentUnit.onStartupError(startupError, newStartupState);
        verify(mResultReceiver, never()).send(eq(1), any(Bundle.class));
    }

    @Test
    public void testGetClientIdentifiersProvider() {
        assertThat(mComponentUnit.getClientIdentifiersProvider()).isSameAs(mClientIdentifiersProvider);
    }

    @Test
    public void requestReferrer() {
        ResultReceiver receiver = mock(ResultReceiver.class);
        mComponentUnit.requestReferrer(receiver);
        ArgumentCaptor<ReferrerChosenListener> listenerCaptor = ArgumentCaptor.forClass(ReferrerChosenListener.class);
        verify(referrerManager).addOneShotListener(listenerCaptor.capture());
        final ReferrerInfo referrerInfo = new ReferrerInfo("referrer", 10, 20, ReferrerInfo.Source.HMS);
        listenerCaptor.getValue().onReferrerChosen(referrerInfo);
        verify(receiver).send(eq(1), argThat(new ArgumentMatcher<Bundle>() {
            @Override
            public boolean matches(Bundle argument) {
                try {
                    return argument.keySet().size() == 1 &&
                        referrerInfo.equals(ReferrerInfo.parseFrom(argument.getByteArray("referrer")));
                } catch (InvalidProtocolBufferNanoException e) {
                    throw new RuntimeException(e);
                }
            }
        }));
    }

    @Test
    public void requestReferrerNullReceiver() {
        mComponentUnit.requestReferrer(null);
        ArgumentCaptor<ReferrerChosenListener> listenerCaptor = ArgumentCaptor.forClass(ReferrerChosenListener.class);
        verify(referrerManager).addOneShotListener(listenerCaptor.capture());
        listenerCaptor.getValue().onReferrerChosen(new ReferrerInfo("referrer", 10, 20, ReferrerInfo.Source.GP));
    }

    private void setUpToRequireStartup() {
        when(mStartupUnit.isStartupRequired()).thenReturn(true);
        when(mStartupUnit.isStartupRequired(mIdentifiers, mClidsForVerification)).thenReturn(true);
        when(mStartupUnit.getStartupState()).thenReturn(TestUtils.createDefaultStartupState());
        mComponentUnit.provokeStartupOrGetCurrentState(mIdentifiersData);
    }

    @NonNull
    private StartupState createStartupWithClientClids(@Nullable Map<String, String> clids) {
        return TestUtils.createDefaultStartupStateBuilder()
            .withLastClientClidsForStartupRequest(StartupUtils.encodeClids(clids))
            .build();
    }
}
