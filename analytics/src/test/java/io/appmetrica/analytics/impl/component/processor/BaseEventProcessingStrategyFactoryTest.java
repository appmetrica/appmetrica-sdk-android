package io.appmetrica.analytics.impl.component.processor;

import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.processor.event.ApplySettingsFromActivationConfigHandler;
import io.appmetrica.analytics.impl.component.processor.event.ExternalAttributionHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportAppEnvironmentClearedHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportAppEnvironmentUpdatedHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportAppOpenHandler;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;
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
import io.appmetrica.analytics.impl.component.processor.factory.ActivationFactory;
import io.appmetrica.analytics.impl.component.processor.factory.CommonConditionalFactory;
import io.appmetrica.analytics.impl.component.processor.factory.CurrentSessionNativeCrashHandlerFactory;
import io.appmetrica.analytics.impl.component.processor.factory.ExternalAttributionFactory;
import io.appmetrica.analytics.impl.component.processor.factory.HandlersFactory;
import io.appmetrica.analytics.impl.component.processor.factory.JustSaveToDataBaseFactory;
import io.appmetrica.analytics.impl.component.processor.factory.PrevSessionUnhandledExceptionFromFileFactory;
import io.appmetrica.analytics.impl.component.processor.factory.PurgeBufferFactory;
import io.appmetrica.analytics.impl.component.processor.factory.RegularFactory;
import io.appmetrica.analytics.impl.component.processor.factory.ReportAppOpenFactory;
import io.appmetrica.analytics.impl.component.processor.factory.ReportingHandlerProvider;
import io.appmetrica.analytics.impl.component.processor.factory.SingleHandlerFactory;
import io.appmetrica.analytics.impl.component.processor.factory.StartFactory;
import io.appmetrica.analytics.impl.component.processor.factory.UnhandledExceptionFactory;
import io.appmetrica.analytics.impl.component.processor.factory.UnhandledExceptionFromFileFactory;
import io.appmetrica.analytics.impl.component.processor.session.ReportSessionStopHandler;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static io.appmetrica.analytics.impl.InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_ACTIVATION;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_ANR;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_CLEARED;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_UPDATED;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_APP_OPEN;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_CLEANUP;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_CUSTOM_EVENT;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_PREV_SESSION_EXCEPTION_UNHANDLED_FROM_FILE;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_PURGE_BUFFER;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_REGULAR;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_SEND_REFERRER;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_SEND_USER_PROFILE;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_START;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_WEBVIEW_SYNC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @see EventProcessorStrategyFactoryTest
 * @see GenericFactoriesTest
 * @see FactoriesTest
 * @see SingleHandlerFactoriesTest
 */
public class BaseEventProcessingStrategyFactoryTest extends CommonTest {

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

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

    @Rule
    public MockedConstructionRule<CommonConditionalFactory> commonConditionalFactoryMockedConstructionRule =
        new MockedConstructionRule<>(CommonConditionalFactory.class);

    EventProcessingStrategyFactory mFactory;
    final InternalEvents mEventType;

    BaseEventProcessingStrategyFactoryTest(InternalEvents eventType) {
        mEventType = eventType;
    }

    @Before
    public void setUp() {
        ComponentUnit unit = mock(ComponentUnit.class);
        ComponentId componentId = mock(ComponentId.class);
        when(componentId.getApiKey()).thenReturn(UUID.randomUUID().toString());
        when(unit.getComponentId()).thenReturn(componentId);
        doReturn(RuntimeEnvironment.getApplication()).when(unit).getContext();
        mFactory = new EventProcessingStrategyFactory(unit);
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class GenericFactoriesTest extends BaseEventProcessingStrategyFactoryTest {

        private static final HashSet<InternalEvents> LISTED_EVENTS =
            new HashSet<InternalEvents>();

        static {
            LISTED_EVENTS.add(EVENT_TYPE_ACTIVATION);
            LISTED_EVENTS.add(EVENT_TYPE_REGULAR);
            LISTED_EVENTS.add(EVENT_TYPE_EXCEPTION_USER_PROTOBUF);
            LISTED_EVENTS.add(EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF);
            LISTED_EVENTS.add(EVENT_TYPE_SEND_REFERRER);
            LISTED_EVENTS.add(EVENT_TYPE_CUSTOM_EVENT);
            LISTED_EVENTS.add(EVENT_TYPE_APP_OPEN);
            LISTED_EVENTS.add(EVENT_TYPE_PURGE_BUFFER);
            LISTED_EVENTS.add(EVENT_TYPE_PREV_SESSION_EXCEPTION_UNHANDLED_FROM_FILE);
            LISTED_EVENTS.add(EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE);
            LISTED_EVENTS.add(EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF);
            LISTED_EVENTS.add(EVENT_TYPE_ANR);
            LISTED_EVENTS.add(EVENT_TYPE_APP_ENVIRONMENT_UPDATED);
            LISTED_EVENTS.add(EVENT_TYPE_APP_ENVIRONMENT_CLEARED);
            LISTED_EVENTS.add(EVENT_TYPE_SEND_USER_PROFILE);
            LISTED_EVENTS.add(EVENT_TYPE_SET_USER_PROFILE_ID);
            LISTED_EVENTS.add(EVENT_TYPE_SEND_REVENUE_EVENT);
            LISTED_EVENTS.add(EVENT_TYPE_SEND_AD_REVENUE_EVENT);
            LISTED_EVENTS.add(EVENT_TYPE_CLEANUP);
            LISTED_EVENTS.add(EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF);
            LISTED_EVENTS.add(EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF);
            LISTED_EVENTS.add(EVENT_TYPE_SEND_ECOMMERCE_EVENT);
            LISTED_EVENTS.add(EVENT_TYPE_WEBVIEW_SYNC);
            LISTED_EVENTS.add(EVENT_TYPE_SET_SESSION_EXTRA);
            LISTED_EVENTS.add(EVENT_CLIENT_EXTERNAL_ATTRIBUTION);
        }

        @ParameterizedRobolectricTestRunner.Parameters(name = "Test for event {0}")
        public static Collection<Object[]> data() {
            ArrayList<Object[]> events = new ArrayList<Object[]>();
            for (InternalEvents event : InternalEvents.values()) {
                events.add(new Object[]{event});
            }
            return events;
        }

        public GenericFactoriesTest(InternalEvents eventType) {
            super(eventType);
        }

        @Test
        public void testMutation() {
            HandlersFactory factory = mock(HandlersFactory.class);
            assertThat(mFactory.getHandlersFactory(mEventType)).isNotEqualTo(factory);
            mFactory.mutateHandlers(mEventType, factory);
            assertThat(mFactory.getHandlersFactory(mEventType)).isEqualTo(factory);
        }

        @Test
        public void testStrategyCreationForNotListedEvents() {
            EventProcessingStrategy<ReportComponentHandler> processingStrategy =
                mFactory.getProcessingStrategy(mEventType.getTypeId());
            ArrayList<ReportComponentHandler> commonExpectedElements = new ArrayList<ReportComponentHandler>();
            ArrayList<Class<? extends ReportComponentHandler>> commonClasses =
                new ArrayList<Class<? extends ReportComponentHandler>>(commonExpectedElements.size());
            new CommonConditionalFactory(mFactory.getHandlersProvider()).addHandlers(mEventType, commonExpectedElements);
            for (ReportComponentHandler handler : commonExpectedElements) {
                commonClasses.add(handler.getClass());
            }
            List<? extends ReportComponentHandler> eventHandlers = processingStrategy.getEventHandlers();
            if (LISTED_EVENTS.contains(mEventType)) {
                assertThat(eventHandlers).extracting("class").
                    containsAll(commonClasses);
                assertThat(eventHandlers.size()).isGreaterThan(commonClasses.size());
            } else {
                assertThat(eventHandlers).extracting("class").
                    containsExactlyElementsOf(commonClasses);
            }
        }

    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static final class FactoriesTest extends BaseEventProcessingStrategyFactoryTest {

        private final Class<? extends HandlersFactory> mFactoryType;

        @ParameterizedRobolectricTestRunner.Parameters(name = "Event with type {0} should have factory {1}")
        public static Collection<Object[]> data() {
            Class<JustSaveToDataBaseFactory> justSaveFactory = JustSaveToDataBaseFactory.class;
            Class<UnhandledExceptionFactory> unhandledExceptionFactory = UnhandledExceptionFactory.class;
            Class<PrevSessionUnhandledExceptionFromFileFactory> prevSessionUnhandledExceptionFromFileFactory =
                PrevSessionUnhandledExceptionFromFileFactory.class;
            return Arrays.asList(new Object[][]{
                {EVENT_TYPE_ACTIVATION, ActivationFactory.class},
                {EVENT_TYPE_START, StartFactory.class},
                {EVENT_TYPE_REGULAR, RegularFactory.class},
                {EVENT_TYPE_EXCEPTION_USER_PROTOBUF, justSaveFactory},
                {EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF, justSaveFactory},
                {EVENT_TYPE_SEND_REFERRER, justSaveFactory},
                {EVENT_TYPE_CUSTOM_EVENT, justSaveFactory},
                {EVENT_TYPE_ANR, justSaveFactory},
                {EVENT_TYPE_APP_OPEN, ReportAppOpenFactory.class},
                {EVENT_TYPE_PURGE_BUFFER, PurgeBufferFactory.class},
                {EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF, unhandledExceptionFactory},
                {EVENT_TYPE_PREV_SESSION_EXCEPTION_UNHANDLED_FROM_FILE, prevSessionUnhandledExceptionFromFileFactory},
                {EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE, UnhandledExceptionFromFileFactory.class},
                {EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF, CurrentSessionNativeCrashHandlerFactory.class},
                {EVENT_TYPE_SEND_USER_PROFILE, justSaveFactory},
                {EVENT_TYPE_SEND_REVENUE_EVENT, justSaveFactory},
                {EVENT_TYPE_SEND_AD_REVENUE_EVENT, justSaveFactory},
                {EVENT_TYPE_SEND_ECOMMERCE_EVENT, justSaveFactory},
                {EVENT_TYPE_WEBVIEW_SYNC, justSaveFactory},
                {EVENT_CLIENT_EXTERNAL_ATTRIBUTION, ExternalAttributionFactory.class},
            });
        }

        public FactoriesTest(InternalEvents eventType, Class<? extends HandlersFactory> factoryType) {
            super(eventType);
            mFactoryType = factoryType;
        }

        @Test
        public void testProperFactory() {
            assertThat(mFactory.getHandlersFactory(mEventType)).isExactlyInstanceOf(mFactoryType);
        }
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class SingleHandlerFactoriesTest extends BaseEventProcessingStrategyFactoryTest {

        private final Class<ReportComponentHandler> mHandlerClass;

        public SingleHandlerFactoriesTest(InternalEvents eventType, Class<ReportComponentHandler> handlerClass) {
            super(eventType);
            mHandlerClass = handlerClass;
        }

        @ParameterizedRobolectricTestRunner.Parameters(name = "Event with type {0} should have handler {1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                {EVENT_TYPE_APP_ENVIRONMENT_UPDATED, ReportAppEnvironmentUpdatedHandler.class},
                {EVENT_TYPE_APP_ENVIRONMENT_CLEARED, ReportAppEnvironmentClearedHandler.class},
                {EVENT_TYPE_SET_USER_PROFILE_ID, UpdateUserProfileIDHandler.class},
            });
        }

        @Test
        public void testHashProperHandler() {
            HandlersFactory factory = mFactory.getHandlersFactory(mEventType);
            assertThat(factory).isExactlyInstanceOf(SingleHandlerFactory.class);
            ArrayList<?> handlers = new ArrayList<Object>();
            factory.addHandlers(handlers);
            assertThat(handlers).extracting("class").containsOnly(mHandlerClass);
        }

    }
}
