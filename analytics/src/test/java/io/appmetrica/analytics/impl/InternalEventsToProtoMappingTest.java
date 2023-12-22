package io.appmetrica.analytics.impl;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class InternalEventsToProtoMappingTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0} to {1}")
    public static Collection<Object[]> data() {
        Collection<Object[]> data = Arrays.asList(new Object[][]{
            {InternalEvents.EVENT_TYPE_ACTIVATION, null},
            {InternalEvents.EVENT_TYPE_ALIVE, EventProto.ReportMessage.Session.Event.EVENT_ALIVE},
            {InternalEvents.EVENT_TYPE_ANR, EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_ANR},
            {InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_CLEARED, null},
            {InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_UPDATED, null},
            {InternalEvents.EVENT_TYPE_APP_FEATURES, EventProto.ReportMessage.Session.Event.EVENT_APP_FEATURES},
            {InternalEvents.EVENT_TYPE_APP_OPEN, EventProto.ReportMessage.Session.Event.EVENT_OPEN},
            {InternalEvents.EVENT_TYPE_APP_UPDATE, EventProto.ReportMessage.Session.Event.EVENT_UPDATE},
            {InternalEvents.EVENT_TYPE_CLEANUP, EventProto.ReportMessage.Session.Event.EVENT_CLEANUP},
            {InternalEvents.EVENT_TYPE_CUSTOM_EVENT, null},
            {InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF, EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_CRASH},
            {InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF, EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_CRASH},
            {InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_INTENT, EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_CRASH},
            {InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE, EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_CRASH},
            {InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF, EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_CRASH},
            {InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF, EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_ERROR},
            {InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF, EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_ERROR},
            {InternalEvents.EVENT_TYPE_FIRST_ACTIVATION, EventProto.ReportMessage.Session.Event.EVENT_FIRST},
            {InternalEvents.EVENT_TYPE_INIT, EventProto.ReportMessage.Session.Event.EVENT_INIT},
            {InternalEvents.EVENT_TYPE_PERMISSIONS, EventProto.ReportMessage.Session.Event.EVENT_PERMISSIONS},
            {InternalEvents.EVENT_TYPE_PURGE_BUFFER, null},
            {InternalEvents.EVENT_TYPE_REQUEST_REFERRER, null},
            {InternalEvents.EVENT_TYPE_REGULAR, EventProto.ReportMessage.Session.Event.EVENT_CLIENT},
            {InternalEvents.EVENT_TYPE_SEND_REFERRER, EventProto.ReportMessage.Session.Event.EVENT_REFERRER},
            {InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT, EventProto.ReportMessage.Session.Event.EVENT_REVENUE},
            {InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT, EventProto.ReportMessage.Session.Event.EVENT_AD_REVENUE},
            {InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT, EventProto.ReportMessage.Session.Event.EVENT_ECOMMERCE},
            {InternalEvents.EVENT_TYPE_SEND_USER_PROFILE, EventProto.ReportMessage.Session.Event.EVENT_PROFILE},
            {InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID, null},
            {InternalEvents.EVENT_TYPE_START, EventProto.ReportMessage.Session.Event.EVENT_START},
            {InternalEvents.EVENT_TYPE_STARTUP, null},
            {InternalEvents.EVENT_TYPE_UNDEFINED, null},
            {InternalEvents.EVENT_TYPE_UPDATE_FOREGROUND_TIME, null},
            {InternalEvents.EVENT_TYPE_UPDATE_PRE_ACTIVATION_CONFIG, null},
            {InternalEvents.EVENT_TYPE_WEBVIEW_SYNC, EventProto.ReportMessage.Session.Event.EVENT_WEBVIEW_SYNC},
            {InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA, null},
            {InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION, EventProto.ReportMessage.Session.Event.EVENT_CLIENT_EXTERNAL_ATTRIBUTION},
            {null, null}
        });

        assert data.size() == InternalEvents.values().length + 1;
        return data;
    }

    @Nullable
    private final InternalEvents mInternalEvents;
    @Nullable
    private final Integer mProtoType;

    @Rule
    public final GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    public InternalEventsToProtoMappingTest(@Nullable InternalEvents internalEvents, @Nullable Integer protoType) {
        mInternalEvents = internalEvents;
        mProtoType = protoType;
    }

    @Test
    public void test() {
        assertThat(ProtobufUtils.internalEventToProto(mInternalEvents)).isEqualTo(mProtoType);
    }
}
