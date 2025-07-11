package io.appmetrica.analytics.impl.component.processor;

import android.content.Context;
import android.content.SharedPreferences;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.processor.event.ApplySettingsFromActivationConfigHandler;
import io.appmetrica.analytics.impl.component.processor.event.ExternalAttributionHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportAppEnvironmentClearedHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportAppEnvironmentUpdatedHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportAppOpenHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportCrashMetaInformation;
import io.appmetrica.analytics.impl.component.processor.event.ReportFeaturesHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportFirstHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportFirstOccurrenceStatusHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportPermissionHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportPrevSessionEventHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportPurgeBufferHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportSaveToDatabaseHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportSessionHandler;
import io.appmetrica.analytics.impl.component.processor.event.SaveInitialUserProfileIDHandler;
import io.appmetrica.analytics.impl.component.processor.event.SavePreloadInfoHandler;
import io.appmetrica.analytics.impl.component.processor.event.SaveSessionExtrasHandler;
import io.appmetrica.analytics.impl.component.processor.event.SubscribeForReferrerHandler;
import io.appmetrica.analytics.impl.component.processor.event.UpdateUserProfileIDHandler;
import io.appmetrica.analytics.impl.component.processor.event.modules.ModulesEventHandler;
import io.appmetrica.analytics.impl.component.processor.factory.ReportingHandlerProvider;
import io.appmetrica.analytics.impl.component.processor.session.ReportSessionStopHandler;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_ACTIVATION;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_CLEARED;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_UPDATED;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_APP_OPEN;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_CLEANUP;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_CUSTOM_EVENT;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_REGULAR;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_START;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @see BaseEventProcessingStrategyFactoryTest
 */
@RunWith(RobolectricTestRunner.class)
public class EventProcessorStrategyFactoryTest extends CommonTest {

    @Mock
    private ComponentUnit mComponent;
    private final String apiKey = UUID.randomUUID().toString();
    private EventProcessingStrategyFactory mEventProcessingStrategyFactory;

    private static final String PACKAGE = "com.test.package";

    @Mock
    private ComponentId mComponentId;

    @Rule
    public final GlobalServiceLocatorRule mGlobalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Rule
    public MockedConstructionRule<ReportingHandlerProvider> reportingHandlerProviderMockedConstructionRule =
        new MockedConstructionRule<>(ReportingHandlerProvider.class, (mock, context) -> {
            when(mock.getReportPurgeBufferHandler()).thenReturn(mock(ReportPurgeBufferHandler.class));
            when(mock.getReportSaveToDatabaseHandler()).thenReturn(mock(ReportSaveToDatabaseHandler.class));
            when(mock.getReportSessionHandler()).thenReturn(mock(ReportSessionHandler.class));
            when(mock.getReportSessionStopHandler()).thenReturn(mock(ReportSessionStopHandler.class));
            when(mock.getReportAppEnvironmentUpdated()).thenReturn(mock(ReportAppEnvironmentUpdatedHandler.class));
            when(mock.getReportAppEnvironmentCleared()).thenReturn(mock(ReportAppEnvironmentClearedHandler.class));
            when(mock.getReportFirstHandler()).thenReturn(mock(ReportFirstHandler.class));
            when(mock.getReportPermissionsHandler()).thenReturn(mock(ReportPermissionHandler.class));
            when(mock.getReportFeaturesHandler()).thenReturn(mock(ReportFeaturesHandler.class));
            when(mock.getUpdateUserProfileIDHandler()).thenReturn(mock(UpdateUserProfileIDHandler.class));
            when(mock.getReportAppOpenHandler()).thenReturn(mock(ReportAppOpenHandler.class));
            when(mock.getReportFirstOccurrenceStatusHandler()).thenReturn(mock(ReportFirstOccurrenceStatusHandler.class));
            when(mock.getReportCrashMetaInformation()).thenReturn(mock(ReportCrashMetaInformation.class));
            when(mock.getSavePreloadInfoHandler()).thenReturn(mock(SavePreloadInfoHandler.class));
            when(mock.getApplySettingsFromActivationConfigHandler())
                .thenReturn(mock(ApplySettingsFromActivationConfigHandler.class));
            when(mock.getSubscribeForReferrerHandler()).thenReturn(mock(SubscribeForReferrerHandler.class));
            when(mock.getSaveInitialUserProfileIDHandler()).thenReturn(mock(SaveInitialUserProfileIDHandler.class));
            when(mock.getModulesEventHandler()).thenReturn(mock(ModulesEventHandler.class));
            when(mock.getSaveSessionExtrasHandler()).thenReturn(mock(SaveSessionExtrasHandler.class));
            when(mock.getExternalAttributionHandler()).thenReturn(mock(ExternalAttributionHandler.class));
            when(mock.getReportPrevSessionEventHandler()).thenReturn(mock(ReportPrevSessionEventHandler.class));
        });

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Context context = TestUtils.createMockedContext();
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(mock(SharedPreferences.class));
        doReturn(PACKAGE).when(context).getPackageName();

        doReturn(context).when(mComponent).getContext();
        when(mComponentId.getApiKey()).thenReturn(apiKey);
        doReturn(mComponentId).when(mComponent).getComponentId();

        mEventProcessingStrategyFactory = new EventProcessingStrategyFactory(mComponent);
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_Undefined() {
        List handlers = getHandlers(InternalEvents.EVENT_TYPE_UNDEFINED);

        assertThat(handlers).containsExactly(
            mEventProcessingStrategyFactory.getHandlersProvider().getModulesEventHandler()
        );
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_Init() {
        List handlers = getHandlers(InternalEvents.EVENT_TYPE_INIT);

        assertThat(handlers).containsExactly(
            mEventProcessingStrategyFactory.getHandlersProvider().getModulesEventHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSessionHandler()
        );
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_Regular() {
        List handlers = getHandlers(InternalEvents.EVENT_TYPE_REGULAR);

        assertThat(handlers).containsExactly(
            mEventProcessingStrategyFactory.getHandlersProvider().getModulesEventHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSessionHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportFirstOccurrenceStatusHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSaveToDatabaseHandler()
        );
    }

    @Test
    public void testProcessingStrategyShouldReturnExpectedHandlers_EventType_ActivityEnd() {
        List handlers = getHandlers(InternalEvents.EVENT_TYPE_UPDATE_FOREGROUND_TIME);

        assertThat(handlers).containsExactly(
            mEventProcessingStrategyFactory.getHandlersProvider().getModulesEventHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSessionHandler()
        );
    }

    @Test
    public void testProcessingStrategyShouldReturnExpectedHandlers_EventType_ExceptionUnhandled_fromProtobuf() {
        List handlers = getHandlers(InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF);

        assertThat(handlers).containsExactly(
            mEventProcessingStrategyFactory.getHandlersProvider().getModulesEventHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSessionHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportPurgeBufferHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSaveToDatabaseHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportCrashMetaInformation(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSessionStopHandler()
        );
    }

    @Test
    public void testProcessingStrategyShouldReturnExpectedHandlers_EventType_ExceptionUserProtobuf() {
        List handlers = getHandlers(InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF);

        assertThat(handlers).containsExactly(
            mEventProcessingStrategyFactory.getHandlersProvider().getModulesEventHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSessionHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSaveToDatabaseHandler()
        );
    }

    @Test
    public void testProcessingStrategyShouldReturnExpectedHandlers_EventType_CustomErrorProtobuf() {
        List handlers = getHandlers(InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF);

        assertThat(handlers).containsExactly(
            mEventProcessingStrategyFactory.getHandlersProvider().getModulesEventHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSessionHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSaveToDatabaseHandler()
        );
    }

    @Test
    public void testProcessingStrategyShouldReturnExpectedHandlers_EventType_Referrer() {
        List handlers = getHandlers(InternalEvents.EVENT_TYPE_SEND_REFERRER);

        assertThat(handlers).containsExactly(
            mEventProcessingStrategyFactory.getHandlersProvider().getModulesEventHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSaveToDatabaseHandler()
        );
    }

    @Test
    public void testProcessingStrategyShouldReturnExpectedHandlers_EventType_Alive() {
        List handlers = getHandlers(InternalEvents.EVENT_TYPE_ALIVE);

        assertThat(handlers).containsExactly(mEventProcessingStrategyFactory.getHandlersProvider().getReportSessionHandler());
    }

    @Test
    public void testProcessingStrategyShouldReturnExpectedHandlers_EventType_PurgeBuffer() {
        List handlers = getHandlers(InternalEvents.EVENT_TYPE_PURGE_BUFFER);

        assertThat(handlers).containsExactly(
            mEventProcessingStrategyFactory.getHandlersProvider().getReportPurgeBufferHandler()
        );
    }

    @Test
    public void testGetProcessingStrategyReturnExpectedHandlers_EventType_AppEnvironmentUpdated() {
        assertThat(getHandlers(EVENT_TYPE_APP_ENVIRONMENT_UPDATED)).containsExactly(
            mEventProcessingStrategyFactory.getHandlersProvider().getModulesEventHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportAppEnvironmentUpdated()
        );
    }

    @Test
    public void testGetProcessingStrategyReturnExpectedHandlers_EventType_AppEnvironmentCleared() {
        assertThat(getHandlers(EVENT_TYPE_APP_ENVIRONMENT_CLEARED)).containsExactly(
            mEventProcessingStrategyFactory.getHandlersProvider().getModulesEventHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportAppEnvironmentCleared()
        );
    }

    @Test
    public void testGetProcessingStrategyReturnExpectedHandlers_EventType_First() {
        assertThat(getHandlers(EVENT_TYPE_ACTIVATION)).containsExactly(
            mEventProcessingStrategyFactory.getHandlersProvider().getModulesEventHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getApplySettingsFromActivationConfigHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getSavePreloadInfoHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getSaveInitialUserProfileIDHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportFirstHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getSubscribeForReferrerHandler()
        );
    }

    @Test
    public void testGetProcessingStrategyReturnExpectedHandler_EventType_ActivityStart() {
        assertThat(getHandlers(EVENT_TYPE_START)).containsExactly(
            mEventProcessingStrategyFactory.getHandlersProvider().getModulesEventHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSessionHandler()
        );
    }

    @Test
    public void testGetProcessingStrategyReturnExpectedHandler_EventType_CustomEvent() {
        assertThat(getHandlers(EVENT_TYPE_CUSTOM_EVENT)).containsExactly(
            mEventProcessingStrategyFactory.getHandlersProvider().getModulesEventHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSessionHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSaveToDatabaseHandler()
        );
    }

    @Test
    public void getProcessingStrategyHandlers_EventType_SetExtra() {
        assertThat(getHandlers(EVENT_TYPE_SET_SESSION_EXTRA))
            .containsExactly(mEventProcessingStrategyFactory.getHandlersProvider().getSaveSessionExtrasHandler());
    }

    @Test
    public void testGetProcessingStrategyReturnExpectedHandler_EventType_ReportAppOpen() {
        assertThat(getHandlers(EVENT_TYPE_APP_OPEN)).containsExactly(
            mEventProcessingStrategyFactory.getHandlersProvider().getModulesEventHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSessionHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportAppOpenHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSaveToDatabaseHandler()
        );
    }

    @Test
    public void testGetProcessingStrategyReturnExpectedHandlers_EventType_Cleanup() {
        assertThat(getHandlers(EVENT_TYPE_CLEANUP)).containsExactly(
            mEventProcessingStrategyFactory.getHandlersProvider().getModulesEventHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSessionHandler(),
            mEventProcessingStrategyFactory.getHandlersProvider().getReportSaveToDatabaseHandler()
        );
    }

    @Test
    public void testReporter() {
        mComponentId = mock(ComponentId.class);
        doReturn(mComponentId).when(mComponent).getComponentId();
        doReturn(PACKAGE).when(mComponentId).getPackage();

        assertThat(getHandlers(EVENT_TYPE_REGULAR, new EventProcessingStrategyFactory(mComponent)))
            .extracting("class").containsOnly(
                ModulesEventHandler.class,
                ReportSessionHandler.class,
                ReportFirstOccurrenceStatusHandler.class,
                ReportSaveToDatabaseHandler.class
            );
    }

    @Test
    public void getProcessingStrategyHandlers_EventType_PrevSessionCrashpadCrash() {
        assertThat(getHandlers(EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF))
            .containsExactly(
                mEventProcessingStrategyFactory.getHandlersProvider().getReportPrevSessionEventHandler(),
                mEventProcessingStrategyFactory.getHandlersProvider().getReportPurgeBufferHandler(),
                mEventProcessingStrategyFactory.getHandlersProvider().getReportSessionStopHandler()
            );
    }

    @Test
    public void getProcessingStrategyHandlers_EventType_CurrentSessionCrashpadCrash() {
        assertThat(getHandlers(EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF))
            .containsExactly(
                mEventProcessingStrategyFactory.getHandlersProvider().getModulesEventHandler(),
                mEventProcessingStrategyFactory.getHandlersProvider().getReportSessionHandler(),
                mEventProcessingStrategyFactory.getHandlersProvider().getReportSaveToDatabaseHandler(),
                mEventProcessingStrategyFactory.getHandlersProvider().getReportPurgeBufferHandler(),
                mEventProcessingStrategyFactory.getHandlersProvider().getReportSessionStopHandler()
            );
    }

    private List getHandlers(final InternalEvents eventType) {
        return getHandlers(eventType, mEventProcessingStrategyFactory);
    }

    private List getHandlers(final InternalEvents eventType, EventProcessingStrategyFactory factory) {
        return factory.getProcessingStrategy(eventType.getTypeId()).getEventHandlers();
    }

}
