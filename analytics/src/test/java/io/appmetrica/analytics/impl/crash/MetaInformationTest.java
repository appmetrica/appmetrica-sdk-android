package io.appmetrica.analytics.impl.crash;

import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class MetaInformationTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "Event type: {0}. Values: \"{1}\"-\"{2}\"")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF.getTypeId(), "jvm", "binder"},
                {InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_INTENT.getTypeId(), "jvm", "intent"},
                {InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE.getTypeId(), "jvm", "file"},
                {InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF.getTypeId(), "jni_native", "file"},
                {InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF.getTypeId(), "jni_native", "file"},
        });
    }

    private final int eventType;
    private final String crashType;
    private final String deliveryMethod;

    public MetaInformationTest(int eventType, String crashType, String deliveryMethod) {
        this.eventType = eventType;
        this.crashType = crashType;
        this.deliveryMethod = deliveryMethod;
    }

    @Test
    public void testType() {
        assertThat(MetaInformation.getMetaInformation(eventType).type).isEqualTo(crashType);
    }

    @Test
    public void testDeliveryMethod() {
        assertThat(MetaInformation.getMetaInformation(eventType).deliveryMethod).isEqualTo(deliveryMethod);
    }

}
