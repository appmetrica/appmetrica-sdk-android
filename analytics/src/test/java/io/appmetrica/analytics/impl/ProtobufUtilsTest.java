package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.component.session.SessionType;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.impl.telephony.SimInfo;
import io.appmetrica.analytics.impl.utils.TimeUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ProtobufUtilsTest extends CommonTest {

    @Rule
    public final GlobalServiceLocatorRule rule = new GlobalServiceLocatorRule();

    @Test
    public void testBuildSessionStartTime() throws Exception {
        try (MockedStatic<TimeUtils> sTimeUtils = Mockito.mockStatic(TimeUtils.class)) {
            final long timestamp = 762834567;
            final long offset = 5638;
            final int timezone = 54;
            final boolean beforeFirstSync = true;

            when(TimeUtils.getTimeZoneOffsetSec(timestamp)).thenReturn(timezone);

            EventProto.ReportMessage.Time time = ProtobufUtils.buildTime(
                timestamp,
                offset,
                beforeFirstSync
            );

            ObjectPropertyAssertions<EventProto.ReportMessage.Time> assertions =
                ObjectPropertyAssertions(time)
                    .withFinalFieldOnly(false);

            assertions.checkField("timestamp", timestamp);
            assertions.checkField("timeZone", timezone);
            assertions.checkField("serverTimeOffset", offset);
            assertions.checkField("obtainedBeforeFirstSynchronization", beforeFirstSync);

            assertions.checkAll();
        }
    }

    @Test
    public void testBuildSimInfo() throws Exception {
        final int simCountryCode = 55;
        final int simNetworkCode = 44;
        final String operatorName = "A1";
        final boolean isNetworkRoaming = true;

        SimInfo simInfo = new SimInfo(simCountryCode, simNetworkCode, isNetworkRoaming, operatorName);
        EventProto.ReportMessage.SimInfo proto = ProtobufUtils.buildSimInfo(simInfo);

        ObjectPropertyAssertions<EventProto.ReportMessage.SimInfo> assertions =
            ObjectPropertyAssertions(proto)
                .withFinalFieldOnly(false);

        assertions.checkField("countryCode", simCountryCode);
        assertions.checkField("operatorId", simNetworkCode);
        assertions.checkField("operatorName", operatorName);
        assertions.checkField("dataRoaming", isNetworkRoaming);

        assertions.checkAll();
    }

    @Test
    public void testBuildSimInfNullables() throws Exception {

        SimInfo simInfo = new SimInfo(null, null, true, null);
        EventProto.ReportMessage.SimInfo proto = ProtobufUtils.buildSimInfo(simInfo);

        ObjectPropertyAssertions<EventProto.ReportMessage.SimInfo> assertions =
            ObjectPropertyAssertions(proto)
                .withFinalFieldOnly(false);

        assertions.checkField("countryCode", 0);
        assertions.checkField("operatorId", 0);
        assertions.checkField("operatorName", "");
        assertions.checkField("dataRoaming", true);

        assertions.checkAll();
    }

    @Test
    public void testBuildSessionDesc() throws Exception {
        final EventProto.ReportMessage.Time time = mock(EventProto.ReportMessage.Time.class);
        final String locale = "by";
        final SessionType sessionType = SessionType.BACKGROUND;
        final int sessionTypeProto = EventProto.ReportMessage.Session.SessionDesc.SESSION_BACKGROUND;

        EventProto.ReportMessage.Session.SessionDesc sessionDesc = ProtobufUtils.buildSessionDesc(locale, sessionType, time);
        ObjectPropertyAssertions<EventProto.ReportMessage.Session.SessionDesc> assertions =
            ObjectPropertyAssertions(sessionDesc)
                .withFinalFieldOnly(false);

        assertions.checkField("startTime", time);
        assertions.checkField("locale", locale);
        assertions.checkField("sessionType", sessionTypeProto);

        assertions.checkAll();
    }
}
