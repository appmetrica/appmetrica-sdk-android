package io.appmetrica.analytics.impl.db.state.converter;

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(RobolectricTestRunner.class)
public class AutoInappCollectingConfigConverterTest extends CommonTest {

    private final AutoInappCollectingConfigConverter converter = new AutoInappCollectingConfigConverter();

    @Test
    public void testDefaultToModel() throws Exception {
        ObjectPropertyAssertions(
                converter.toModel(new StartupStateProtobuf.StartupState.AutoInappCollectingConfig())
        )
                .checkField("sendFrequencySeconds", 86400)
                .checkField("firstCollectingInappMaxAgeSeconds", 86400)
                .checkAll();
    }

    @Test
    public void testFilledToProto() throws Exception {
        final int sendFrequencySeconds = 43875;
        final int firstCollectingInappMaxAgeSeconds =  783476;
        BillingConfig model = new BillingConfig(sendFrequencySeconds, firstCollectingInappMaxAgeSeconds);
        new ProtoObjectPropertyAssertions<StartupStateProtobuf.StartupState.AutoInappCollectingConfig>(converter.fromModel(model))
                .checkField("sendFrequencySeconds", sendFrequencySeconds)
                .checkField("firstCollectingInappMaxAgeSeconds", firstCollectingInappMaxAgeSeconds)
                .checkAll();
    }

    @Test
    public void testFilledProtoToModel() throws Exception {
        final int sendFrequencySeconds = 43875;
        final int firstCollectingInappMaxAgeSeconds =  783476;
        StartupStateProtobuf.StartupState.AutoInappCollectingConfig proto =
                new StartupStateProtobuf.StartupState.AutoInappCollectingConfig();
        proto.sendFrequencySeconds = sendFrequencySeconds;
        proto.firstCollectingInappMaxAgeSeconds = firstCollectingInappMaxAgeSeconds;
        ObjectPropertyAssertions(converter.toModel(proto))
                .checkField("sendFrequencySeconds", sendFrequencySeconds)
                .checkField("firstCollectingInappMaxAgeSeconds", firstCollectingInappMaxAgeSeconds)
                .checkAll();
    }
}
