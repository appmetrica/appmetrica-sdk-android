package io.appmetrica.analytics.impl.referrer.common;

import io.appmetrica.analytics.impl.protobuf.client.ReferrerInfoClient;
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ReferrerInfoGeneralTest extends CommonTest {

    @Test
    public void testConstructor() {
        String referrer = "some_ref";
        long install = 1000;
        long click = 15509;

        ReferrerInfo info = new ReferrerInfo(
                referrer,
                click,
                install,
                ReferrerInfo.Source.HMS
        );

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(info.installReferrer).as("Referrer").isEqualTo(referrer);
        assertions.assertThat(info.referrerClickTimestampSeconds).as("Click timestamp").isEqualTo(click);
        assertions.assertThat(info.installBeginTimestampSeconds).as("Install begin").isEqualTo(install);
        assertions.assertThat(info.source).as("Source").isEqualTo(ReferrerInfo.Source.HMS);
        assertions.assertAll();
    }

    @Test
    public void deserializationFromBadSource() throws InvalidProtocolBufferNanoException {
        ReferrerInfoClient proto = new ReferrerInfoClient();
        proto.source = 88;
        ReferrerInfo restored = ReferrerInfo.parseFrom(MessageNano.toByteArray(proto));
        assertThat(restored.source).isEqualTo(ReferrerInfo.Source.UNKNOWN);
    }
}
