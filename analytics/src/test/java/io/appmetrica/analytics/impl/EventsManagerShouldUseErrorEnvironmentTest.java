package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class EventsManagerShouldUseErrorEnvironmentTest extends CommonTest {

    private static final List<Integer> EVENT_TYPES_WITH_ERROR_ENVIRONMENT = Arrays.asList(
            InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF.getTypeId(),
            InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF.getTypeId(),
            InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF.getTypeId(),
            InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE.getTypeId(),
            InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_INTENT.getTypeId(),
            InternalEvents.EVENT_TYPE_ANR.getTypeId()
    );

    @ParameterizedRobolectricTestRunner.Parameters(name = "For arguments {0} expected value is {1}")
    public static Collection<Object[]> data() {
        List<Object[]> data = new ArrayList<Object[]>();
        for (InternalEvents internalEvent : InternalEvents.values()) {
            data.add(new Object[]{internalEvent.getTypeId(), EVENT_TYPES_WITH_ERROR_ENVIRONMENT.contains(internalEvent.getTypeId())});
        }
        return data;
    }

    private final int mEventType;
    private final boolean mExpected;

    public EventsManagerShouldUseErrorEnvironmentTest(final int eventType, final boolean expected) {
        mEventType = eventType;
        mExpected = expected;
    }

    @Test
    public void testShouldUseErrorEnvironment() {
        assertThat(EventsManager.shouldUseErrorEnvironment(mEventType)).isEqualTo(mExpected);
    }
}
