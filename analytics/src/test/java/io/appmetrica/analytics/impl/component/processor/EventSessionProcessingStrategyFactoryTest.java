package io.appmetrica.analytics.impl.component.processor;

import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_ACTIVATION;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_ALIVE;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_CLEARED;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_UPDATED;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_INIT;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_PREV_SESSION_EXCEPTION_UNHANDLED_FROM_FILE;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_PURGE_BUFFER;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_REGULAR;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_SEND_REFERRER;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_START;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_STARTUP;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_UNDEFINED;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_UPDATE_FOREGROUND_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class EventSessionProcessingStrategyFactoryTest extends CommonTest {

    private ComponentUnit mComponent;
    private EventSessionProcessingStrategyFactory mProcessingFactory;

    @Rule
    public final GlobalServiceLocatorRule mGlobalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        mComponent = mock(ComponentUnit.class);
        mProcessingFactory = new EventSessionProcessingStrategyFactory(mComponent);
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_Undefined() {
        assertThat(getHandlers(EVENT_TYPE_UNDEFINED)).isEmpty();
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_Init() {
        assertThat(getHandlers(EVENT_TYPE_INIT)).containsExactly(
            mProcessingFactory.getReportSaveInitHandler()
        );
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_Regular() {
        assertThat(getHandlers(EVENT_TYPE_REGULAR)).isEmpty();
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_ActivityEnd() {
        assertThat(getHandlers(EVENT_TYPE_UPDATE_FOREGROUND_TIME)).containsExactly(
            mProcessingFactory.getReportPauseForegroundSessionHandler()
        );
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_ExceptionUserProtobuf() {
        assertThat(getHandlers(EVENT_TYPE_EXCEPTION_USER_PROTOBUF)).isEmpty();
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_CustomErrorProtobuf() {
        assertThat(getHandlers(EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF)).isEmpty();
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_ReferrerDeprecated() {
        assertThat(getHandlers(EVENT_TYPE_SEND_REFERRER)).isEmpty();
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_Alive() {
        assertThat(getHandlers(EVENT_TYPE_ALIVE)).isEmpty();
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_PurgeBuffer() {
        assertThat(getHandlers(EVENT_TYPE_PURGE_BUFFER)).isEmpty();
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_UnhandledFromFile() {
        assertThat(getHandlers(EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE)).isEmpty();
    }

    @Test
    public void getProcessingStrategyShouldReturnExpectedHandlers_EventType_PrevSessionUnhandledFromFile() {
        assertThat(getHandlers(EVENT_TYPE_PREV_SESSION_EXCEPTION_UNHANDLED_FROM_FILE)).isEmpty();
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_Startup() {
        assertThat(getHandlers(EVENT_TYPE_STARTUP)).isEmpty();
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_AppEnvironmentUpdated() {
        assertThat(getHandlers(EVENT_TYPE_APP_ENVIRONMENT_UPDATED)).isEmpty();
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandler_EventType_AppEnvironmentCleared() {
        assertThat(getHandlers(EVENT_TYPE_APP_ENVIRONMENT_CLEARED)).isEmpty();
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_ExceptionUnhandledProtobuf() {
        assertThat(getHandlers(EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF)).isEmpty();
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_First() {
        assertThat(getHandlers(EVENT_TYPE_ACTIVATION)).isEmpty();
    }

    @Test
    public void testGetProcessingStrategyShouldReturnExpectedHandlers_EventType_ActivityStart() {
        assertThat(getHandlers(EVENT_TYPE_START)).containsExactly(
            mProcessingFactory.getReportSessionActivityStartHandler(),
            mProcessingFactory.getReportSaveInitHandler()
        );
    }

    private List getHandlers(final InternalEvents eventType) {
        EventProcessingStrategy<ReportComponentHandler> strategy = mProcessingFactory
            .getProcessingStrategy(eventType.getTypeId());

        return strategy.getEventHandlers();
    }
}
