package io.appmetrica.analytics.impl.preparer;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

public class FullNetworkInfoComposerTest extends CommonTest {

    @Rule
    public final GlobalServiceLocatorRule rule = new GlobalServiceLocatorRule();

    private FullNetworkInfoComposer composer;

    @Before
    public void setUp() throws Exception {
        composer = new FullNetworkInfoComposer();
    }

    @Test
    public void buildNetworkInfoFilled() throws Exception {
        final Random random = new Random();

        final int connectionType = 4769;
        final String cellularConnectionType = "some type";

        EventProto.ReportMessage.Session.Event.NetworkInfo proto = composer.getNetworkInfo(
            connectionType,
            cellularConnectionType
        );

        ProtoObjectPropertyAssertions<EventProto.ReportMessage.Session.Event.NetworkInfo> assertions =
            new ProtoObjectPropertyAssertions<EventProto.ReportMessage.Session.Event.NetworkInfo>(proto);

        assertions.checkField("connectionType", connectionType);
        assertions.checkField("cellularNetworkType", cellularConnectionType);
        assertions.checkAll();
    }

    @Test
    public void buildNetworkInfo() throws Exception {
        final int connectionType = 4769;
        final String cellularConnectionType = "some type";

        EventProto.ReportMessage.Session.Event.NetworkInfo proto = composer.getNetworkInfo(
            connectionType,
            cellularConnectionType
        );

        ObjectPropertyAssertions<EventProto.ReportMessage.Session.Event.NetworkInfo> assertions =
            ObjectPropertyAssertions(proto)
                .withFinalFieldOnly(false);

        assertions.checkField("connectionType", connectionType);
        assertions.checkField("cellularNetworkType", cellularConnectionType);

        assertions.checkAll();

    }

    @Test
    public void buildNetworkInfoNullables() throws Exception {
        EventProto.ReportMessage.Session.Event.NetworkInfo proto = composer
            .getNetworkInfo(null, null);

        ObjectPropertyAssertions<EventProto.ReportMessage.Session.Event.NetworkInfo> assertions =
            ObjectPropertyAssertions(proto)
                .withFinalFieldOnly(false);

        assertions.checkField("connectionType", EventProto.ReportMessage.Session.CONNECTION_UNDEFINED);
        assertions.checkField("cellularNetworkType", "");

        assertions.checkAll();
    }
}
