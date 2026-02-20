package io.appmetrica.analytics.impl.referrer.common;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.impl.protobuf.client.ReferrerInfoClient;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(Parameterized.class)
public class ReferrerInfoSerializationTest extends CommonTest {

    @Parameters(name = "{0} to {1}")
    public static Collection<Object[]> data() {
        List<Object[]> data = Arrays.asList(
            new Object[]{ReferrerInfo.Source.UNKNOWN, ReferrerInfoClient.UNKNOWN},
            new Object[]{ReferrerInfo.Source.GP, ReferrerInfoClient.GP},
            new Object[]{ReferrerInfo.Source.HMS, ReferrerInfoClient.HMS}
        );
        assert data.size() == ReferrerInfo.Source.values().length;
        return data;
    }

    private final String referrer = "test_referrer";
    private final long clickTimestamp = 48578796668L;
    private final long installTimestamp = 999898098098L;
    @NonNull
    private final ReferrerInfo.Source modelSource;
    private final int protoSource;

    public ReferrerInfoSerializationTest(@NonNull ReferrerInfo.Source modelSource,
                                         int protoSource) {
        this.modelSource = modelSource;
        this.protoSource = protoSource;
    }

    @Test
    public void serialization() throws Exception {
        ReferrerInfo referrerInfo = new ReferrerInfo(referrer, clickTimestamp, installTimestamp, modelSource);
        new ProtoObjectPropertyAssertions<ReferrerInfoClient>(ReferrerInfoClient.parseFrom(referrerInfo.toProto()))
            .checkField("value", referrer)
            .checkField("clickTimeSeconds", clickTimestamp)
            .checkField("installBeginTimeSeconds", installTimestamp)
            .checkField("source", protoSource)
            .checkAll();
    }

    @Test
    public void deserialization() throws Exception {
        ReferrerInfoClient proto = new ReferrerInfoClient();
        proto.value = referrer;
        proto.clickTimeSeconds = clickTimestamp;
        proto.installBeginTimeSeconds = installTimestamp;
        proto.source = protoSource;
        ObjectPropertyAssertions(ReferrerInfo.parseFrom(MessageNano.toByteArray(proto)))
            .checkField("installReferrer", referrer)
            .checkField("referrerClickTimestampSeconds", clickTimestamp)
            .checkField("installBeginTimestampSeconds", installTimestamp)
            .checkField("source", modelSource)
            .checkAll();
    }

    @Test
    public void thereAndBackAgain() throws Exception {
        ReferrerInfo referrerInfo = new ReferrerInfo(referrer, clickTimestamp, installTimestamp, modelSource);
        ObjectPropertyAssertions(ReferrerInfo.parseFrom(referrerInfo.toProto()))
            .checkField("installReferrer", referrer)
            .checkField("referrerClickTimestampSeconds", clickTimestamp)
            .checkField("installBeginTimestampSeconds", installTimestamp)
            .checkField("source", modelSource)
            .checkAll();
    }
}
